package net.dodian.uber.game.netty.listener.in;

import net.dodian.uber.game.engine.metrics.PacketRejectTelemetry;
import net.dodian.uber.game.engine.systems.net.PacketRejectReason;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;

/**
 * Frames and rejects Tarnish packets whose corresponding gameplay feature is
 * intentionally unavailable. Keeping these bindings explicit prevents an
 * unsupported feature from being mistaken for protocol desynchronization.
 */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {23, 79, 136, 149, 156, 181, 187, 218, 253})
public final class UnsupportedTarnishPacketListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        PacketRejectTelemetry.record(packet.opcode(), PacketRejectReason.OPCODE_DISABLED);
        packet.payload().skipBytes(packet.payload().readableBytes());
    }
}
