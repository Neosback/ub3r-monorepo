package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opcode 73 – player attacking another player.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {73})
public class AttackPlayerListener implements PacketListener {
  private static final Logger logger = LoggerFactory.getLogger(AttackPlayerListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }
        int victimSlot = msg.playerIndex();

        if (logger.isTraceEnabled()) {
            logger.trace("AttackPlayer from={} victimSlot={}", client.getPlayerName(), victimSlot);
        }

        PacketInteractionService.handleAttackPlayer(client, packet.opcode(), victimSlot);
    }
}