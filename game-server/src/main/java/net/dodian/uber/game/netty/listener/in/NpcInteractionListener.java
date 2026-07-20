package net.dodian.uber.game.netty.listener.in;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.Server;
import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.engine.systems.net.PacketRejectReason;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;
import net.dodian.uber.game.engine.systems.net.PacketInteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Netty handler for npc interaction opcodes:
 * 155 (slot 0/click1), 17 (slot 2/click3), 21 (slot 3/click4), 18 (legacy slot 4/click4),
 * 72 (attack/slot 1).
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {155, 17, 21, 18, 72})
public class NpcInteractionListener implements PacketListener {

    private static final Logger logger = LoggerFactory.getLogger(NpcInteractionListener.class);
    @Override
    public void handle(Client client, GamePacket packet) {
        switch (packet.opcode()) {
            case 155:
                handleNpcClick1(client, packet);
                return;
            case 17:
                handleNpcClick3(client, packet);
                return;
            case 21:
                handleNpcClick4(client, packet);
                return;
            case 18:
                handleNpcClick4LegacySlot(client, packet);
                return;
            case 72:
                handleNpcAttack(client, packet);
                return;
            default:
                logger.warn("NpcInteractionListener got unexpected opcode={} for player={}", packet.opcode(), client.getPlayerName());
        }
    }

    private void handleNpcClick1(Client client, GamePacket packet) {
        int npcIndex = decodeNpcIndex(packet);
        if (npcIndex < 0) {
            return;
        }
        PacketInteractionService.handleNpcClick(client, packet.opcode(), 1, npcIndex);
    }

    private void handleNpcClick3(Client client, GamePacket packet) {
        int npcIndex = decodeNpcIndex(packet);
        if (npcIndex < 0) {
            return;
        }
        PacketInteractionService.handleNpcClick(client, packet.opcode(), 3, npcIndex);
    }

    private void handleNpcClick4(Client client, GamePacket packet) {
        int npcIndex = decodeNpcIndex(packet);
        if (npcIndex < 0) {
            return;
        }
        PacketInteractionService.handleNpcClick(client, packet.opcode(), 4, npcIndex);
    }

    private void handleNpcClick4LegacySlot(Client client, GamePacket packet) {
        int npcIndex = decodeNpcIndex(packet);
        if (npcIndex < 0) {
            return;
        }
        PacketInteractionService.handleNpcClick(client, packet.opcode(), 4, npcIndex);
    }

    private void handleNpcAttack(Client client, GamePacket packet) {
        int npcIndex = decodeNpcIndex(packet);
        if (net.dodian.uber.game.engine.config.DotEnvKt.getGameWorldId() == 2) {
            logger.info("[W2-ATTACK-LISTENER] handleNpcAttack: opcode={}, decodedNpcIndex={}, raw_bytes={}", packet.opcode(), npcIndex, io.netty.buffer.ByteBufUtil.hexDump(packet.payload()));
        }
        if (npcIndex < 0) {
            return;
        }
        logger.debug("Npc attack opcode={} npcIndex={} player={}", packet.opcode(), npcIndex, client.getPlayerName());
        PacketInteractionService.handleNpcAttack(client, packet.opcode(), npcIndex);
    }

    /** Decodes via the per-opcode typed table; returns -1 for malformed payload or unknown npc. */
    private int decodeNpcIndex(GamePacket packet) {
        net.dodian.uber.game.netty.game.decode.TarnishPackets.NpcClick msg =
                net.dodian.uber.game.netty.game.decode.TarnishPackets.NpcClick.decode(packet.opcode(), packet.payload());
        if (msg == null) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.MALFORMED_PAYLOAD);
            if (net.dodian.uber.game.engine.config.DotEnvKt.getGameWorldId() == 2) {
                logger.info("[W2-ATTACK-LISTENER] decodeNpcIndex failed: msg is null");
            }
            return -1;
        }
        if (!isKnownNpcIndex(msg.npcIndex())) {
            PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.UNKNOWN_NPC);
            if (net.dodian.uber.game.engine.config.DotEnvKt.getGameWorldId() == 2) {
                logger.info("[W2-ATTACK-LISTENER] decodeNpcIndex failed: npcIndex={} is not known (containsKey={})", msg.npcIndex(), Server.npcManager != null && Server.npcManager.getNpcMap().containsKey(msg.npcIndex()));
            }
            return -1;
        }
        return msg.npcIndex();
    }

    private static boolean isKnownNpcIndex(int npcIndex) {
        return npcIndex >= 0 && Server.npcManager != null && Server.npcManager.getNpcMap().containsKey(npcIndex);
    }
}
