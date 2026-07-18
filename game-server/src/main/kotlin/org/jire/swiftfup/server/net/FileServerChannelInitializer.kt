package org.jire.swiftfup.server.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.jire.swiftfup.server.net.codec.HandshakeRequestDecoder
import org.jire.swiftfup.server.net.codec.HandshakeRequestHandler

class FileServerChannelInitializer(
    private val version: Int,
    private val fileResponses: FileResponses,
    private val protectionConfig: FileServerProtectionConfig,
    private val protectionRegistry: FileServerProtectionRegistry,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
            .addLast(FileServerProtectionHandler.TIMEOUT_HANDLER_NAME, FileServerProtectionHandler.timeout(protectionConfig))
            .addLast(FileServerProtectionHandler.HANDLER_NAME, FileServerProtectionHandler(protectionRegistry))
            .addLast(DECODER_HANDLER, HandshakeRequestDecoder())
            .addLast(TAIL_HANDLER, HandshakeRequestHandler(version, fileResponses))
    }

    companion object {
        const val DECODER_HANDLER = "decoder"
        const val TAIL_HANDLER = "tail"
    }

}
