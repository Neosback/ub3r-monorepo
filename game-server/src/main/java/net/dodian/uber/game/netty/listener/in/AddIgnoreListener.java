package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketSocialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {133})
public class AddIgnoreListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(AddIgnoreListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        long ig = buf.readLong();
        logger.debug("AddIgnoreListener: {} ignores {}", client.getPlayerName(), ig);
        PacketSocialService.handleAddIgnore(client, ig);
    }
}