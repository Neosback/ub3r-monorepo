package net.dodian.uber.game.network.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LoginDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LogManager.getLogger(LoginDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Log the amount of data received.
        // In a real implementation, we would check if enough data is available to parse a complete login packet.
        // For example: if (in.readableBytes() < REQUIRED_BYTES) { return; }
        // Then, we would read data from the ByteBuf 'in', construct a message object, and add it to 'out'.
        // e.g., LoginRequest loginRequest = parseLoginRequest(in);
        // if (loginRequest != null) { out.add(loginRequest); }

        logger.info("LoginDecoder received " + in.readableBytes() + " bytes from " + ctx.channel().remoteAddress());

        // For now, this is a placeholder. We are just consuming all readable bytes to prevent the buffer from growing indefinitely.
        // In a real scenario, you'd only consume bytes when a full message is decoded.
        // If you don't consume (read) bytes from 'in' and don't add anything to 'out',
        // this method might be called again with more data, or ByteToMessageDecoder might complain
        // if the buffer keeps growing without producing messages.
        if (in.readableBytes() > 0) {
            // Placeholder: Skip all readable bytes.
            // In a real decoder, you would read specific parts of the buffer based on your protocol.
            // logger.debug("Consuming all " + in.readableBytes() + " readable bytes as a placeholder.");
            // in.skipBytes(in.readableBytes());
            
            // It's often better to wait for enough data for a full packet.
            // For now, if we don't have a specific packet structure, just logging is fine.
            // The ByteToMessageDecoder will accumulate data until out.add() is called or an exception is thrown.
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in LoginDecoder for " + ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close the connection on error
    }
}
