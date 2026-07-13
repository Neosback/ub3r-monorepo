package net.dodian.uber.game.netty.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.dodian.uber.game.engine.config.DotEnvKt.getNettyLeakDetection;

public class NettyGameServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyGameServer.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    public interface EpollAvailabilityChecker {
        boolean isAvailable();
        Throwable unavailabilityCause();
    }

    public static final EpollAvailabilityChecker DEFAULT_CHECKER = new EpollAvailabilityChecker() {
        @Override
        public boolean isAvailable() {
            return Epoll.isAvailable();
        }

        @Override
        public Throwable unavailabilityCause() {
            return Epoll.unavailabilityCause();
        }
    };

    private final int port;
    private final EpollAvailabilityChecker epollChecker;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyGameServer(int port) {
        this(port, DEFAULT_CHECKER);
    }

    public NettyGameServer(int port, EpollAvailabilityChecker epollChecker) {
        this.port = port;
        this.epollChecker = epollChecker;
    }

    public void start() {
        String configuredLevel;
        try {
            configuredLevel = System.getProperty("netty.leakDetection", getNettyLeakDetection());
        } catch (Throwable t) {
            configuredLevel = System.getProperty("netty.leakDetection", "disabled");
        }
        ResourceLeakDetector.Level leakDetectionLevel = resolveLeakDetectionLevel(configuredLevel);
        ResourceLeakDetector.setLevel(leakDetectionLevel);
        logger.info("[Netty] Resource leak detection {} ({})", leakDetectionLevel, describeLeakDetectionSource(configuredLevel));

        boolean useEpoll = epollChecker.isAvailable();
        Class<? extends ServerChannel> channelClass;
        if (useEpoll) {
            logger.info("[Netty] Using Epoll transport.");
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            Throwable cause = epollChecker.unavailabilityCause();
            logger.info("[Netty] Epoll transport is unavailable, falling back to NIO. Reason: {}",
                cause != null ? cause.toString() : "Unsupported platform/OS");
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childHandler(new GameChannelInitializer())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        logger.info("[Netty] Binding game server on port {}", port);
        ChannelFuture bindFuture = bootstrap.bind(port).syncUninterruptibly();
        serverChannel = bindFuture.channel();
        logger.info("[Netty] Game server listening on {}", port);

    }

    public void shutdown() {
        logger.info("[Netty] Shutting down game server");
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            logger.warn("Error closing server channel", e);
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully(0, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(0, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    static ResourceLeakDetector.Level resolveLeakDetectionLevel(String configuredLevel) {
        if (configuredLevel == null || configuredLevel.isBlank()) {
            return ResourceLeakDetector.Level.DISABLED;
        }

        switch (configuredLevel.trim().toLowerCase()) {
            case "disabled":
            case "off":
            case "false":
                return ResourceLeakDetector.Level.DISABLED;
            case "simple":
                return ResourceLeakDetector.Level.SIMPLE;
            case "advanced":
                return ResourceLeakDetector.Level.ADVANCED;
            case "paranoid":
            case "true":
                return ResourceLeakDetector.Level.PARANOID;
            default:
                throw new IllegalArgumentException("Unsupported NETTY_LEAK_DETECTION value: " + configuredLevel);
        }
    }

    private static String describeLeakDetectionSource(String configuredLevel) {
        if (configuredLevel == null || configuredLevel.isBlank() || "disabled".equalsIgnoreCase(configuredLevel)) {
            return "default disabled";
        }
        return "explicit override";
    }
}
