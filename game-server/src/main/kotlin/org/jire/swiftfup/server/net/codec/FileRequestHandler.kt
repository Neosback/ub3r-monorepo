package org.jire.swiftfup.server.net.codec

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.FilePair.Companion.writeFilePair
import org.jire.swiftfup.server.net.FileResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Jire
 */
class FileRequestHandler(
    private val responses: FileResponses,
) : SimpleChannelInboundHandler<FilePair>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FilePair) {
        val response = responses[msg]
        if (response == null) {
            val remote = (ctx.channel().remoteAddress() as? java.net.InetSocketAddress)
                ?.address?.hostAddress ?: ctx.channel().remoteAddress().toString()
            org.jire.swiftfup.server.net.SwiftFupDiagnostics.missingResponse(
                remote,
                msg,
                responses.presentAtStartup(msg),
            )

            val byteBufSize = FilePair.SIZE_BYTES + 4
            val byteBuf = Unpooled.directBuffer(byteBufSize, byteBufSize)
                .writeFilePair(msg)
                .writeInt(0)

            ctx.write(byteBuf, ctx.voidPromise())
        } else {
            org.jire.swiftfup.server.net.SwiftFupDiagnostics.responseServed(response.readableBytes())
            ctx.write(response.retainedDuplicate(), ctx.voidPromise())
        }
        if (!ctx.channel().isWritable) {
            ctx.channel().config().isAutoRead = false
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        org.jire.swiftfup.server.net.SwiftFupDiagnostics.channelFailure()
        logger.error("Exception caught from remote \"${ctx.channel().remoteAddress()}\"", cause)

        ctx.close()
    }

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(FileRequestHandler::class.java)

    }

}
