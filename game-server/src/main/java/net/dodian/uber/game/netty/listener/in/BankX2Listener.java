package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketBankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the second half of an "X" withdraw/deposit (opcode 208).
 * Reads the entered amount and delegates the actual container mutation logic.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {208})
public class BankX2Listener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(BankX2Listener.class);
    private static final int MIN_PAYLOAD_BYTES = 4;

    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.EnteredAmount msg = net.dodian.uber.game.netty.game.decode.TarnishPackets.EnteredAmount.decode(packet.payload());
        if (msg == null || msg.amount() < 1) {
            return;
        }
        int enteredAmount = msg.amount();

        if (logger.isTraceEnabled()) {
            logger.trace("BankX2 amount={} iface={} removeId={} slot={} player={}", enteredAmount,
                    client.XinterfaceID, client.XremoveID, client.XremoveSlot, client.getPlayerName());
        }

        PacketBankingService.handleXAmount(client, enteredAmount);
    }
}