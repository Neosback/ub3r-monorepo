package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketSocialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {188})
public class AddFriendListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(AddFriendListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        net.dodian.uber.game.netty.game.decode.TarnishPackets.EncodedName msg = net.dodian.uber.game.netty.game.decode.TarnishPackets.EncodedName.decode(buf);
        if (msg == null) {
            return;
        }
        long friend = msg.name();
        logger.debug("AddFriendListener: {} adds {}", client.getPlayerName(), friend);
        PacketSocialService.handleAddFriend(client, friend);
    }
}