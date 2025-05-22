package net.dodian.uber.game.network.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Encoder extends MessageToByteEncoder<Object> { // Using Object as a placeholder type

    private static final Logger logger = LogManager.getLogger(Encoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // Log the attempt to encode a message.
        // In a real implementation, we would check the type of 'msg' and then
        // write its data to the ByteBuf 'out'.
        // For example:
        // if (msg instanceof PlayerPositionPacket) {
        //     PlayerPositionPacket packet = (PlayerPositionPacket) msg;
        //     out.writeInt(packet.getPlayerId());
        //     out.writeInt(packet.getX());
        //     out.writeInt(packet.getY());
        // } else if (msg instanceof ChatMessagePacket) {
        //     ChatMessagePacket packet = (ChatMessagePacket) msg;
        //     byte[] messageBytes = packet.getMessage().getBytes(StandardCharsets.UTF_8);
        //     out.writeByte(messageBytes.length);
        //     out.writeBytes(messageBytes);
        // }
        // else {
        //     logger.warn("Unknown message type to encode: " + msg.getClass().getName());
        //     return; // Or throw an exception
        // }

        logger.info("Encoder called for message: " + msg.getClass().getSimpleName() + " for " + ctx.channel().remoteAddress());

        // For now, this is a placeholder. We are not writing any bytes to 'out'.
        // If no bytes are written to 'out', Netty will not send anything.
        // This is acceptable for a placeholder, but in a real encoder,
        // this method must write data to 'out' for the message to be sent.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in Encoder for " + ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close the connection on error
    }
}
