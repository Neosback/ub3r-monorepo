package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {129})
public class BankAllListener implements PacketListener {
    private static final int PAYLOAD_BYTES = 6;

    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.BankPresetAction msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.BankPresetAction.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }

        PacketBankingService.handleBankAllDecoded(client, msg.interfaceId(), msg.slot(), msg.itemId());
    }

}
