package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInteractionRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {153})
public class DuelRequestListener implements PacketListener {
    private static final Logger logger = LoggerFactory.getLogger(DuelRequestListener.class);

    @Override
    public void handle(Client client, GamePacket packet) {
        // Tarnish client writes: writeLEShort(index). The old multiplier-loop decode only
        // recovered the low byte (temp/1000 discards the high byte's contribution), silently
        // truncating any player index >= 256 to index & 0xFF — fixed to a plain LE short read.
        net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.PlayerMenuClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            return;
        }
        int pid = msg.playerIndex();

        Client other = client.getClient(pid);
        if (!client.validClient(pid) || client.getSlot() == pid) {
            return;
        }
        logger.debug("{} sent duel request to {} (slot {})", client.getPlayerName(), other.getPlayerName(), pid);
        PacketInteractionRequestService.handleDuelRequest(client, pid, other);
    }
}