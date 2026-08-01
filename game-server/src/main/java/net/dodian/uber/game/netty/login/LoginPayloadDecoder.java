package net.dodian.uber.game.netty.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Decodes the second stage RuneScape 317 login block.
 * <p>
 * Packet layout after the initial opcode-14 handshake is:
 * <pre>
 *     byte loginType        (16 = reconnecting, 18 = new login)
 *     byte loginPacketSize  (N, legacy value two bytes short)
 *     byte[N + 2] payload   (starts with RSA_MAGIC 255)
 * </pre>
 * <p>
 * This decoder waits until the full <code>N</code> byte payload is available
 * and then forwards it as a {@link LoginPayload} so that the heavy parsing
 * is handled by {@link LoginProcessorHandler}.
 */
public class LoginPayloadDecoder extends ByteToMessageDecoder {

    /**
     * Tarnish retained the original +37 length formula after changing the
     * client-version field from four bytes to one. Its original server also
     * consumed two bytes beyond the declared block size.
     */
    static final int TARNISH_LENGTH_CORRECTION = 2;

    private int payloadSize = -1;
    private boolean reconnecting;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (payloadSize == -1) {
            if (in.readableBytes() < 2) {
                return; // Need loginType + size
            }
            int loginType = in.readUnsignedByte();
            if (loginType != 16 && loginType != 18) {
                ctx.close();
                return;
            }
            reconnecting = loginType == 18;
            payloadSize = in.readUnsignedByte() + TARNISH_LENGTH_CORRECTION;
        }

        if (in.readableBytes() < payloadSize) {
            return; // Wait for full payload
        }

        ByteBuf payload = in.readRetainedSlice(payloadSize);
        out.add(new LoginPayload(payload, reconnecting));

        // Reset for safety (should not expect multiple login payloads)
        payloadSize = -1;
    }
}
