package net.dodian.uber.game.netty.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.netty.protocol.TarnishProtocol;
import net.dodian.utilities.ISAACCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Decodes ISAAC-obfuscated Tarnish client packets. Only the opcode is obfuscated. */
public final class GamePacketDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(GamePacketDecoder.class);
    public static final AttributeKey<ISAACCipher> IN_CIPHER_KEY = AttributeKey.valueOf("inCipher");

    private int opcode = -1;
    private int size;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ISAACCipher cipher = ctx.channel().attr(IN_CIPHER_KEY).get();
        if (cipher == null) {
            PacketRejectTelemetry.record(-1, "missing_cipher");
            logger.warn("Missing inbound ISAAC cipher; closing remote={}", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        while (true) {
            if (opcode == -1) {
                if (!in.isReadable()) return;
                opcode = (in.readUnsignedByte() - cipher.getNextKey()) & 0xff;
                size = TarnishProtocol.inboundSize(opcode);
            }

            if (size == TarnishProtocol.VARIABLE_BYTE) {
                if (!in.isReadable()) return;
                size = in.readUnsignedByte();
            } else if (size == TarnishProtocol.VARIABLE_SHORT) {
                if (in.readableBytes() < Short.BYTES) return;
                size = in.readUnsignedShort();
            }

            if (in.readableBytes() < size) return;
            ByteBuf payload = size == 0 ? ctx.alloc().buffer(0, 0) : in.readRetainedSlice(size);
            out.add(new GamePacket(opcode, size, payload));
            opcode = -1;
            size = 0;
            if (!in.isReadable()) return;
        }
    }
}
