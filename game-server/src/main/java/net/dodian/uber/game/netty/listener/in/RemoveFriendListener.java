package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.netty.listener.PacketListenerManager;
import net.dodian.uber.game.engine.systems.net.PacketSocialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoveFriendListener implements PacketListener {

    static { PacketListenerManager.register(215, new RemoveFriendListener()); }

    private static final Logger logger = LoggerFactory.getLogger(RemoveFriendListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        long friend = buf.readLong();
        logger.debug("RemoveFriendListener: {} removes {}", client.getPlayerName(), friend);
        PacketSocialService.handleRemoveFriend(client, friend);
    }
}