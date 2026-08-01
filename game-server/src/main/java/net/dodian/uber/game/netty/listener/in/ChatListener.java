package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.DecodedPublicChat;
import net.dodian.uber.game.netty.codec.PublicChatCodec;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native Netty listener for public chat messages (opcode 4).
 * Decodes color, effects and chat string, then delegates to PacketChatService.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {4})
public class ChatListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatListener.class);
    private static final int MIN_PAYLOAD_BYTES = 3;
    @Override
    public void handle(Client client, GamePacket packet) throws Exception {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < MIN_PAYLOAD_BYTES) {
            return;
        }

        byte[] payload = new byte[buf.readableBytes()];
        buf.readBytes(payload);
        DecodedPublicChat decoded = PublicChatCodec.decode(payload);
        if (decoded == null) {
            return;
        }

        PacketChatService.handlePublicChat(
                client,
                decoded.getColor(),
                decoded.getEffects(),
                decoded.getMessage()
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Chat from {}: {}", client.getPlayerName(), decoded.getMessage());
        }
    }
}
