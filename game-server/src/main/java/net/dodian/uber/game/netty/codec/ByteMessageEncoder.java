package net.dodian.uber.game.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.dodian.utilities.ISAACCipher;
import net.dodian.uber.game.netty.protocol.TarnishProtocol;
import java.util.List;

/**
 * Encodes ByteMessage instances into ByteBuf instances.
 * Extends MessageToMessageEncoder to support true zero-copy writing of both the header and the payload.
 * Encodes the immutable Tarnish client's fixed/variable packet framing.
 */
public class ByteMessageEncoder extends MessageToMessageEncoder<ByteMessage> {

    private final ISAACCipher cipher;

    public ByteMessageEncoder() {
        this.cipher = null;
    }

    public ByteMessageEncoder(ISAACCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteMessage bm, List<Object> out) throws Exception {
        int opcode = bm.getOpcode();
        ByteBuf payload = bm.content();
        int length = payload.readableBytes();

        MessageType type = bm.getType();
        if (type == MessageType.RAW) {
            payload.retain();
            out.add(payload);
            return;
        }

        TarnishProtocol.validateOutbound(opcode, type, length);
        int headerSize = type == MessageType.FIXED ? 1 : type == MessageType.VAR ? 2 : 3;
        ByteBuf header = ctx.alloc().buffer(headerSize);
        int encOpcode = cipher == null ? opcode : (opcode + cipher.getNextKey()) & 0xFF;
        header.writeByte(encOpcode);
        if (type == MessageType.VAR) {
            header.writeByte(length);
        } else if (type == MessageType.VAR_SHORT) {
            header.writeShort(length);
        }
        out.add(header);

        if (length > 0) {
            payload.retain();
            out.add(payload);
        }
    }
}
