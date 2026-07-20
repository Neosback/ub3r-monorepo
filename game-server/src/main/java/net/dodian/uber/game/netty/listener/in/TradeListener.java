package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opcode 139 now carries slot-4 player menu clicks, which are used for Follow.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {139})
public class TradeListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(TradeListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        // NOTE: the Tarnish client sends opcode 139 for the "Trade" menu action, but this
        // server's 139/128/39 handlers are deliberately cross-wired as a compensating set
        // (139=Follow, 128=Trade, 39=Trade) — confirmed working in-game. Do not "fix" the
        // routing without retesting all three player-menu actions together.
        net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }
        int targetSlot = msg.playerIndex();
        PlayerClickListener.handleFollowPlayer(client, targetSlot);
        if (logger.isTraceEnabled()) {
            logger.trace("{} sent Follow request to slot {}", client.getPlayerName(), targetSlot);
        }
    }
}