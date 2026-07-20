package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opcode 25 – Item on ground item. The legacy handler only parsed the packet
 * and printed debug; there is no gameplay logic attached. We preserve the same
 * behaviour here (TRACE-level log for debugging) so the bridge can be removed.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {25})
public class ItemOnGroundItemListener implements PacketListener {
  private static final Logger logger = LoggerFactory.getLogger(ItemOnGroundItemListener.class);
    private static final int PAYLOAD_BYTES = 12;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < PAYLOAD_BYTES) {
            return;
        }

        // Tarnish client writes 6 shorts: LEShort, ShortA, Short, ShortA, LEShortA, Short.
        // Field semantics aren't confirmed (this handler is debug-only, no gameplay attached),
        // but the byte layout now matches the client's actual write instead of the previous
        // mismatched mixed-width decode.
        int field1 = ByteBufReader.readShortSigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
        int field2 = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
        int field3 = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
        int field4 = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
        int field5 = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
        int field6 = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);

        if (logger.isTraceEnabled()) {
            logger.trace("ItemOnGroundItem field1={} field2={} field3={} field4={} field5={} field6={} player={}",
                    field1, field2, field3, field4, field5, field6, client.getPlayerName());
        }
        // No further behaviour in legacy implementation.
    }
}