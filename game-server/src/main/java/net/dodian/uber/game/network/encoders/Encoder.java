package net.dodian.uber.game.network.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.dodian.uber.game.network.message.LoginResponseMessage; // Import LoginResponseMessage
import net.dodian.utilities.IsaacCipher; // Import IsaacCipher
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Encoder extends MessageToByteEncoder<LoginResponseMessage> {

    private static final Logger logger = LogManager.getLogger(Encoder.class);

    // ISAAC Ciphers for packet encryption/decryption
    // These will be initialized after the handshake is complete.
    // For login response, these are not used. They will be used for game packets.
    private IsaacCipher incomingCipher; // For symmetry with Decoder
    private IsaacCipher outgoingCipher; // For encrypting outgoing game packets

    @Override
    protected void encode(ChannelHandlerContext ctx, LoginResponseMessage msg, ByteBuf out) throws Exception {
        logger.info("Encoding LoginResponseMessage: {} for {}", msg, ctx.channel().remoteAddress());

        out.writeByte(msg.getResponseCode());

        if (msg.getResponseCode() == 2) { // 2 is typically the code for successful login
            out.writeByte(msg.getPlayerRights());
            out.writeByte(msg.getFlaggedStatus());
            // The client also expects 2 more bytes after this for a successful login, often ignored or 0.
            // Based on typical 317 protocol:
            // out.writeByte(0); // Often referred to as "client index" related, but might be ignored or part of player id.
            // out.writeByte(0); // Another byte, could be related to "is member" or similar.
            // For now, sticking to the explicit fields from LoginResponseMessage.
            // If the client disconnects after this, it might be expecting more bytes.
            // The provided spec only mentions responseCode, playerRights, and flaggedStatus.
        }
        // If the response code is not 2, only the response code byte is sent.
        // The client will typically close the connection or display a message based on this code.

        logger.debug("Encoded {} bytes for LoginResponseMessage (Code: {}) to {}",
                     out.writerIndex(), msg.getResponseCode(), ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in Encoder for " + ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close the connection on error
    }

    // Methods to set ciphers, to be called after successful login and ISAAC key exchange
    public void setIncomingCipher(IsaacCipher incomingCipher) {
        this.incomingCipher = incomingCipher;
    }

    public void setOutgoingCipher(IsaacCipher outgoingCipher) {
        this.outgoingCipher = outgoingCipher;
    }
}
