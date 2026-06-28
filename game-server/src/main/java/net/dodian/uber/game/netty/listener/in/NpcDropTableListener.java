package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.content.DropDisplay;
import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.engine.systems.net.PacketRejectReason;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketHandler;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.netty.listener.PacketListenerManager;
import net.dodian.uber.game.netty.listener.out.ShowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the client-side NPC "Loot table" menu action.
 *
 * The Tarnish client sends opcode 26 as a length byte followed by the NPC name.
 */
@PacketHandler(opcode = 26)
public final class NpcDropTableListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(NpcDropTableListener.class);
    private static final int MAX_NPC_NAME_LENGTH = 64;

    static {
        PacketListenerManager.register(26, new NpcDropTableListener());
    }

    @Override
    public void handle(Client client, GamePacket packet) {
        ByteBuf payload = packet.payload();
        if (payload.readableBytes() < 2) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.SHORT_PAYLOAD);
            return;
        }

        int encodedLength = payload.readUnsignedByte();
        int maxLength = Math.min(MAX_NPC_NAME_LENGTH, Math.min(payload.readableBytes(), Math.max(0, encodedLength - 1)));
        String npcName = ByteBufReader.readTerminatedString(payload, maxLength).trim();
        if (npcName.isEmpty()) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            logger.warn("NPC drop table packet had empty name size={} player={}", packet.size(), client.getPlayerName());
            return;
        }

        if (DropDisplay.search(client, npcName, DropDisplay.DropType.NPC)) {
            client.send(new ShowInterface(54500));
        }
    }
}
