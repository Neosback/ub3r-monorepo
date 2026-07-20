package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {141})
public class BankWithdrawRememberedXListener implements PacketListener {
    private static final int MIN_PAYLOAD_BYTES = 10;

    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.BankWithdrawRememberedX msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.BankWithdrawRememberedX.decode(packet.payload());
        if (msg == null || msg.amount() < 1) {
            return;
        }

        PacketBankingService.handleFixedAmountDecoded(client, msg.interfaceId(), msg.itemId(), msg.slot(), msg.amount());
    }
}