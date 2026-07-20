package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {241})
public class MouseClicksListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(MouseClicksListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.MouseClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.MouseClick.decode(packet.payload());
        if (msg == null) {
            return;
        }
        int clickId = msg.packed();

        String env = System.getenv().getOrDefault("SERVER_ENV", "");
        if ("dev".equalsIgnoreCase(env)) {
            logger.debug("MouseClicks id {} from {}", clickId, client.getPlayerName());
        }
    }
}