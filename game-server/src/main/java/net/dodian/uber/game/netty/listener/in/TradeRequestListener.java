package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInteractionRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {128})
public class TradeRequestListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(TradeRequestListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        // NOTE: the Tarnish client sends opcode 128 for duel-request clicks, but this server's
        // 139/128/39 handlers are deliberately cross-wired as a compensating set (139=Follow,
        // 128=Trade, 39=Trade) — confirmed working in-game. See TradeListener.
        net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }
        int targetSlot = msg.playerIndex();
        Client other = client.getClient(targetSlot);
        if (!client.validClient(targetSlot) || client.getSlot() == targetSlot) {
            return;
        }
        PacketInteractionRequestService.handleLegacyTradeRequest(client, targetSlot, other);
        if (logger.isTraceEnabled()) {
            logger.trace("{} sent TradeRequest to slot {} ({})", client.getPlayerName(), targetSlot, other.getPlayerName());
        }
    }
}