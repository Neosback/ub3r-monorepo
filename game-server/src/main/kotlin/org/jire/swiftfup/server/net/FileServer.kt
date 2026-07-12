package org.jire.swiftfup.server.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.util.ResourceLeakDetector
import org.slf4j.LoggerFactory

/**
 * @author Jire
 */
class FileServer(
    private val version: Int,
    private val fileResponses: FileResponses
) {
    private val logger = LoggerFactory.getLogger(FileServer::class.java)
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var channelFuture: ChannelFuture? = null

    fun start(port: Int) {
        val useEpoll = Epoll.isAvailable()
        bossGroup = if (useEpoll) EpollEventLoopGroup(1) else NioEventLoopGroup(1)
        workerGroup = if (useEpoll) EpollEventLoopGroup() else NioEventLoopGroup()
        
        val channelClass = if (useEpoll) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java
        
        val bootstrap = ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(channelClass)
            .childOption(ChannelOption.AUTO_READ, true)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_RCVBUF, 65536)
            .childOption(ChannelOption.SO_SNDBUF, 65536)
            .childOption(
                ChannelOption.WRITE_BUFFER_WATER_MARK,
                WriteBufferWaterMark(2 shl 20, 2 shl 24)
            )
            .childHandler(FileServerChannelInitializer(version, fileResponses))
            
        logger.info("[SwiftFUP] Binding SwiftFUP server on port {}", port)
        channelFuture = bootstrap.bind(port).sync()
        logger.info("[SwiftFUP] SwiftFUP server listening on {}", port)
    }

    fun shutdown() {
        logger.info("[SwiftFUP] Shutting down SwiftFUP server")
        try {
            channelFuture?.channel()?.close()?.syncUninterruptibly()
        } catch (e: Exception) {
            logger.warn("Error closing SwiftFUP server channel", e)
        } finally {
            bossGroup?.shutdownGracefully()
            workerGroup?.shutdownGracefully()
        }
    }
}
