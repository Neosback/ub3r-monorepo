package net.dodian.uber.game.netty.bootstrap;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.dodian.uber.game.engine.config.DotEnvKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class ChannelIpFilter extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChannelIpFilter.class);
    private final ConcurrentHashMap<String, AtomicInteger> ipCounters = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String host = hostFrom(ctx);
        if (host == null || host.equals("127.0.0.1") || host.startsWith("192.168.") || host.startsWith("10.")) {
            ctx.fireChannelRegistered();
            return;
        }

        int limit = DotEnvKt.getGameConnectionsPerIp();
        int count = ipCounters.computeIfAbsent(host, k -> new AtomicInteger()).incrementAndGet();
        if (count > limit) {
            ipCounters.get(host).decrementAndGet();
            logger.warn("Connection rejected from {} (exceeds limit of {})", host, limit);
            ctx.close();
            return;
        }

        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String host = hostFrom(ctx);
        if (host != null && !host.equals("127.0.0.1") && !host.startsWith("192.168.") && !host.startsWith("10.")) {
            AtomicInteger counter = ipCounters.get(host);
            if (counter != null) {
                counter.decrementAndGet();
                ipCounters.remove(host, 0);
            }
        }

        ctx.fireChannelUnregistered();
    }

    private static String hostFrom(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
            return addr != null ? addr.getAddress().getHostAddress() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
