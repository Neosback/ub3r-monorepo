package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInterfaceCloseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {130})
public class ClickingStuffListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(ClickingStuffListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        // The original handler ignored the payload (a signed byte). We just advance if present.
        ByteBuf buf = packet.payload();
        if (buf.isReadable()) buf.readByte();

        logger.debug("ClickingStuffListener triggered for player {}", client.getPlayerName());

        PacketInterfaceCloseService.handle(client);
    }
}