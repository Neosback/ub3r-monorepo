package net.dodian.uber.game.netty.listener.in;
import net.dodian.uber.game.api.content.ContentInteraction;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.interaction.PlayerTickThrottleService;
import net.dodian.uber.game.engine.systems.net.PacketConnectionService;
import net.dodian.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {95})
public class UpdateChatListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(UpdateChatListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        // Legacy packet structure: [byte toggle?][byte privateChat][byte unknown]
        buf.readUnsignedByte();
        int priv = buf.readUnsignedByte();
        buf.readUnsignedByte();

        if (!ContentInteraction.tryAcquireMs(client, ContentInteraction.CHAT_PRIVACY, 600L)) {
            return; // anti-spam
        }
        PacketConnectionService.setPrivateChatMode(client, priv);

        // Notify friends so their list icon updates
        for (int i = 0; i < PlayerRegistry.players.length; i++) {
            Client other = client.getClient(i);
            if (client.validClient(i) && other.hasFriend(Utils.playerNameToInt64(client.getPlayerName()))) {
                other.refreshFriends();
            }
        }
        logger.debug("UpdateChatListener: {} set private chat={} and refreshed friends", client.getPlayerName(), priv);
    }
}
