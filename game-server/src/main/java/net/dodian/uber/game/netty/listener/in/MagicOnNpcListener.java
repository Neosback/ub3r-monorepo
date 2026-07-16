package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketMagicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {131})
public class MagicOnNpcListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(MagicOnNpcListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf buf = packet.payload();
        if (buf.readableBytes() < 4) {
            return;
        }

        int npcIndex = ByteBufReader.readShortSigned(buf, ByteOrder.LITTLE, ValueType.ADD);
        int magicId = ByteBufReader.readShortSigned(buf, ByteOrder.BIG, ValueType.ADD);

        logger.debug("MagicOnNpcListener: magic {} on npc {}", magicId, npcIndex);

        PacketMagicService.handleMagicOnNpc(client, packet.opcode(), npcIndex, magicId);
    }
}