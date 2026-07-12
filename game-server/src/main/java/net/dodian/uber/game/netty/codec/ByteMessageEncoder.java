package net.dodian.uber.game.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.dodian.utilities.ISAACCipher;
import java.util.List;

/**
 * Encodes ByteMessage instances into ByteBuf instances.
 * Extends MessageToMessageEncoder to support true zero-copy writing of both the header and the payload.
 * Unconditionally writes a 16-bit short for the payload length, matching this client's protocol.
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

        ByteBuf header = ctx.alloc().buffer(3);
        int encOpcode = cipher == null ? opcode : (opcode + cipher.getNextKey()) & 0xFF;
        header.writeByte(encOpcode);
        header.writeShort(length);
        out.add(header);

        if (length > 0) {
            payload.retain();
            out.add(payload);
        }
    }
}