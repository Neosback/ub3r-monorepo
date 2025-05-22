package net.dodian.uber.game.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.dodian.utilities.IsaacCipher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GamePacketDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LogManager.getLogger(GamePacketDecoder.class);
    private final IsaacCipher inboundCipher;

    public GamePacketDecoder(IsaacCipher inboundCipher) {
        if (inboundCipher == null) {
            throw new NullPointerException("inboundCipher");
        }
        this.inboundCipher = inboundCipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Placeholder decode logic for game packets.
        // In a real implementation, this would:
        // 1. Decrypt the packet opcode (if opcodes are encrypted).
        // 2. Read the packet opcode (1 byte).
        // 3. Read the packet size (if applicable, e.g., short - 2 bytes).
        // 4. Wait until enough bytes are available for the full packet (opcode + size + payload).
        // 5. Read the payload.
        // 6. Decrypt the payload using the inboundCipher.
        // 7. Construct a game packet message object.
        // 8. Add the message object to the 'out' list.

        if (in.readableBytes() == 0) {
            return; // Nothing to decode
        }

        logger.debug("GamePacketDecoder received {} readable bytes from {}. Encrypted packet data (hex): {}",
                     in.readableBytes(), ctx.channel().remoteAddress(), ByteBufUtil.hexDump(in));

        // For now, as a placeholder, we'll just consume all bytes to see data flow.
        // This is NOT how a real decoder would work, as it needs to parse specific packet structures.
        // byte[] data = new byte[in.readableBytes()];
        // in.readBytes(data);
        // logger.info("Consumed {} bytes in GamePacketDecoder (placeholder).", data.length);

        // A proper implementation would look more like:
        // if (in.readableBytes() < 1) return; // Need at least 1 byte for opcode
        // int encryptedOpcode = in.readUnsignedByte();
        // int opcode = (encryptedOpcode - inboundCipher.getNextKey()) & 0xFF;
        //
        // if (in.readableBytes() < SIZES[opcode]) return; // SIZES is an array of packet lengths
        // ByteBuf payload = in.readBytes(SIZES[opcode]);
        // GamePacket gamePacket = new GamePacket(opcode, payload); // Payload would be decrypted here or by handler
        // out.add(gamePacket);
        // payload.release(); // If not automatically handled by GamePacket or if GamePacket doesn't retain it.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception in GamePacketDecoder for " + ctx.channel().remoteAddress(), cause);
        ctx.close(); // Close the connection on error
    }
}
