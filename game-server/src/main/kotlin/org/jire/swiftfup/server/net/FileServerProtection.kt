package org.jire.swiftfup.server.net

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.util.AttributeKey
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory

data class FileServerProtectionConfig @JvmOverloads constructor(
    val connectionsPerIp: Int = 8,
    val readTimeoutSeconds: Int = 30,
    val maxTrackedIps: Int = 10_000,
)

/**
 * Bounded application-layer protection for the cache-update port. This limits abusive channels;
 * volumetric DDoS protection still belongs at the host/provider firewall.
 */
class FileServerProtectionRegistry(private val config: FileServerProtectionConfig) {
    private val states = ConcurrentHashMap<String, IpState>()

    fun register(ctx: ChannelHandlerContext): Boolean {
        val address = host(ctx) ?: return false
        val existing = states[address]
        val state =
            existing ?: run {
                if (states.size >= config.maxTrackedIps) {
                    evictExpired(System.currentTimeMillis())
                    if (states.size >= config.maxTrackedIps) return false
                }
                states.putIfAbsent(address, IpState()) ?: states[address]!!
            }
        val now = System.currentTimeMillis()
        synchronized(state) {
            state.lastSeenMillis = now
            if (now - state.connectionWindowStartMillis >= WINDOW_MILLIS) {
                state.connectionWindowStartMillis = now
                state.connectionAttempts = 0
            }
            state.connectionAttempts++
            if (state.connectionAttempts > maxOf(16, config.connectionsPerIp * 4)) {
                return false
            }
        }
        val count = state.connections.incrementAndGet()
        if (count > config.connectionsPerIp) {
            state.connections.decrementAndGet()
            return false
        }
        ctx.channel().attr(REGISTERED_IP).set(address)
        SwiftFupDiagnostics.connectionAccepted()
        return true
    }

    fun release(ctx: ChannelHandlerContext) {
        val address = ctx.channel().attr(REGISTERED_IP).getAndSet(null) ?: return
        val state = states[address] ?: return
        state.connections.decrementAndGet()
        state.lastSeenMillis = System.currentTimeMillis()
        SwiftFupDiagnostics.connectionClosed()
    }

    internal fun trackedIpCount(): Int = states.size

    private fun evictExpired(nowMillis: Long) {
        for ((address, state) in states) {
            if (state.connections.get() == 0 && nowMillis - state.lastSeenMillis >= STATE_TTL_MILLIS) {
                states.remove(address, state)
            }
        }
    }

    private fun host(ctx: ChannelHandlerContext): String? =
        (ctx.channel().remoteAddress() as? InetSocketAddress)?.address?.hostAddress

    private class IpState {
        val connections = AtomicInteger()
        @Volatile var lastSeenMillis = System.currentTimeMillis()
        var connectionWindowStartMillis = lastSeenMillis
        var connectionAttempts = 0
    }

    private companion object {
        const val WINDOW_MILLIS = 1_000L
        const val STATE_TTL_MILLIS = 60_000L
        val REGISTERED_IP: AttributeKey<String> = AttributeKey.valueOf("swiftfup.registered-ip")
    }
}

class FileServerProtectionHandler(
    private val registry: FileServerProtectionRegistry,
) : ChannelInboundHandlerAdapter() {
    override fun channelActive(ctx: ChannelHandlerContext) {
        if (!registry.register(ctx)) {
            logger.warn("swiftfup_connection_rejected remote={} reason=ip-capacity", ctx.channel().remoteAddress())
            ctx.close()
            return
        }
        ctx.fireChannelActive()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        registry.release(ctx)
        ctx.fireChannelInactive()
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        val writable = ctx.channel().isWritable
        ctx.channel().config().isAutoRead = writable
        if (writable) {
            ctx.read()
        }
        ctx.fireChannelWritabilityChanged()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause !== ReadTimeoutException.INSTANCE) {
            SwiftFupDiagnostics.channelFailure()
            logger.debug("SwiftFUP channel failure remote={}", ctx.channel().remoteAddress(), cause)
        } else {
            SwiftFupDiagnostics.timeout()
        }
        ctx.close()
    }

    companion object {
        const val HANDLER_NAME = "swiftfup-protection"
        const val TIMEOUT_HANDLER_NAME = "swiftfup-read-timeout"
        private val logger = LoggerFactory.getLogger(FileServerProtectionHandler::class.java)

        fun timeout(config: FileServerProtectionConfig) = ReadTimeoutHandler(config.readTimeoutSeconds)
    }
}
