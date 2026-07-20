package net.dodian.uber.game.netty.listener.in;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the focus change packet (opcode 3).
 * This packet is sent when the client window gains or loses focus.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {3})
public class FocusChangeListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(FocusChangeListener.class);
    @Override
    public void handle(Client client, GamePacket packet) {
        try {
            net.dodian.uber.game.netty.game.decode.TarnishPackets.FocusChange msg =
                    net.dodian.uber.game.netty.game.decode.TarnishPackets.FocusChange.decode(packet.payload());
            if (msg == null) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Client {} focus state changed to: {}", client.getPlayerName(), msg.focused());
            }
            PacketConnectionService.handleFocusChange(client, msg.focused());
        } catch (Exception e) {
            logger.error("Error handling focus change packet for " + client.getPlayerName(), e);
        }
    }
}
