package net.dodian.uber.game.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.dodian.uber.game.network.decoders.LoginDecoder;
import net.dodian.uber.game.network.encoders.Encoder;
import net.dodian.uber.game.network.handlers.NettyLoginHandler; // Added NettyLoginHandler import
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyServer {

    private static final Logger logger = LogManager.getLogger(NettyServer.class);

    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture serverChannelFuture;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1); // Single thread for boss group to accept connections
        workerGroup = new NioEventLoopGroup(); // Default number of threads for worker group to handle I/O

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     logger.info("Initializing channel for login: {}", ch.remoteAddress());
                     // Inbound handlers (processed head to tail)
                     ch.pipeline().addLast("loginDecoder", new LoginDecoder());
                     ch.pipeline().addLast("loginHandler", new NettyLoginHandler());

                     // Outbound handlers (processed tail to head)
                     // Encoder is placed last here, meaning it's first for outbound messages.
                     ch.pipeline().addLast("encoder", new Encoder());

                     logger.info("Login channel pipeline configured for {}: LoginDecoder -> NettyLoginHandler -> Encoder", ch.remoteAddress());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // Maximum queue length for incoming connections
             .childOption(ChannelOption.SO_KEEPALIVE, true); // Keep connections alive

            // Bind and start to accept incoming connections.
            serverChannelFuture = b.bind(port).sync();
            logger.info("Netty server started and listening on port " + port);

        } catch (Exception e) {
            logger.error("Error starting Netty server", e);
            stop(); // Ensure groups are shut down if start fails
            throw e; // Re-throw to signal failure
        }
    }

    public void stop() {
        logger.info("Stopping Netty server...");
        try {
            if (serverChannelFuture != null) {
                serverChannelFuture.channel().close().syncUninterruptibly();
                logger.info("Server channel closed.");
            }
        } finally {
            if (workerGroup != null && !workerGroup.isShuttingDown() && !workerGroup.isShutdown()) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
                logger.info("Worker group shut down.");
            }
            if (bossGroup != null && !bossGroup.isShuttingDown() && !bossGroup.isShutdown()) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
                logger.info("Boss group shut down.");
            }
        }
        logger.info("Netty server stopped.");
    }

    // Placeholder ChannelInitializer, to be expanded later
    private static class GameChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // This is where the pipeline will be configured with handlers
            logger.info("GameChannelInitializer: Initializing channel for " + ch.remoteAddress());
            // ch.pipeline().addLast(...); // Example: add your handlers here
        }
    }

    public static void main(String[] args) throws Exception {
        // This is a basic test main method.
        // In a real application, this would be managed by the main game server class.
        int port = 43594; // Default Dodian port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid port number specified: " + args[0] + ". Using default port " + port);
            }
        }

        NettyServer server = new NettyServer(port);
        try {
            server.start();
            // Keep the server running until shutdown is triggered, e.g., by a SIGTERM or other mechanism
            // For this basic example, we'll just let it run and rely on a manual stop or JVM termination.
            // In a real app, you might have serverChannelFuture.channel().closeFuture().sync();
            // but that would block the main thread here, preventing a clean shutdown via server.stop()
            // from another thread without interruption.
            Thread.currentThread().join(); // Keep main thread alive
        } catch (InterruptedException e) {
            logger.warn("Server start interrupted.", e);
            Thread.currentThread().interrupt(); // Restore interruption status
        } finally {
            server.stop();
        }
    }
}
