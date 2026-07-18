package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {43})
public class Bank10Listener implements PacketListener {
    private static final int PAYLOAD_BYTES = 6;

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() != PAYLOAD_BYTES) {
            return;
        }

        int[] decoded = decode(buf);
        int interfaceId = decoded[0];
        int removeId = decoded[1];
        int removeSlot = decoded[2];

        PacketBankingService.handleFixedAmountDecoded(client, interfaceId, removeId, removeSlot, 10);
    }

    static int[] decode(ByteBuf buf) {
        return new int[] {
                ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL),
                ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD),
                ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD)
        };
    }
}
