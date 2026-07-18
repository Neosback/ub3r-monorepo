package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {145})
public class RemoveItemListener implements PacketListener {
    private static final int PAYLOAD_BYTES = 6;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() != PAYLOAD_BYTES) {
            return;
        }

        // Tarnish first item action: interface, slot and item are all unsigned
        // big-endian shorts with ADD applied to the low byte.
        int[] decoded = decode(buf);
        int interfaceID = decoded[0];
        int removeSlot = decoded[1];
        int removeID = decoded[2];

        PacketBankingService.handleRemoveItemDecoded(client, interfaceID, removeSlot, removeID);
    }

    static int[] decode(ByteBuf buf) {
        return new int[] {
                ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD),
                ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD),
                ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD)
        };
    }
}
