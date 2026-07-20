package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInteractionRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opcode 39 – slot-5 player menu click (Trade with in current menu mapping).
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {39})
public class FollowPlayerListener implements PacketListener {
  private static final Logger logger = LoggerFactory.getLogger(FollowPlayerListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        // NOTE: the Tarnish client sends opcode 39 for the "Follow" menu action, but this
        // server's 139/128/39 handlers are deliberately cross-wired as a compensating set
        // (139=Follow, 128=Trade, 39=Trade) — confirmed working in-game. See TradeListener.
        net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }
        int followId = msg.playerIndex();

        if (logger.isTraceEnabled()) {
            logger.trace("Trade request from={} targetSlot={}", client.getPlayerName(), followId);
        }

        Client other = client.getClient(followId);
        if (!client.validClient(followId) || client.getSlot() == followId) {
            return;
        }

        PacketInteractionRequestService.handleTradeRequest(client, followId, other);
    }
}