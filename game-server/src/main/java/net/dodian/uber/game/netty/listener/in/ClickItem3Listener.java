package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.engine.systems.interaction.items.ItemDispatcher;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketItemActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty handler for opcode 75 (third click on inventory item).
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {75})
public class ClickItem3Listener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(ClickItem3Listener.class);
    private static final int MIN_PAYLOAD_BYTES = 6;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < MIN_PAYLOAD_BYTES) {
            return;
        }

        // Tarnish client (action==847) writes: LEShortA interfaceId, LEShort slot, ShortA itemId.
        int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
        int itemSlot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
        int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);

        logger.debug("ClickItem3Listener: slot {} item {}", itemSlot, itemId);

        if (!PacketItemActionService.validateInventorySlot(client, itemSlot)) return;
        if (client.playerItems[itemSlot] - 1 != itemId) {
            return;
        }
        if (client.randomed || client.UsingAgility) return;

        ItemDispatcher.tryHandle(client, 3, itemId, itemSlot, interfaceId);
    }
}
