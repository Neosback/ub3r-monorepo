package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {121, 210})
public class ChangeRegionListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(ChangeRegionListener.class);
    @Override
    public void handle(Client client, GamePacket packet) throws Exception {
        // No payload to read; ensure we consume any bytes if size>0 just in case
        ByteBuf buf = packet.payload();
        if (buf.isReadable()) {
            buf.skipBytes(buf.readableBytes());
        }

        PacketConnectionService.handleRegionChange(client, packet.opcode() == 121);
    }
}
