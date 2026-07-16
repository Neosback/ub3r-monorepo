package net.dodian.uber.game.netty.listener.in;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles opcode 0 keep-alive packets (no data).
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {0})
public final class KeepAliveListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveListener.class);
    @Override
    public void handle(Client client, GamePacket packet) {
        PacketConnectionService.handleKeepAlive(client);
    }
}
