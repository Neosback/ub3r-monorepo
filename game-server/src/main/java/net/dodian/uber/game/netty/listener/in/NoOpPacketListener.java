package net.dodian.uber.game.netty.listener.in;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.game.GamePacket;
import net.dodian.uber.game.netty.listener.PacketListener;

/** Explicitly consumes client packets that are valid but intentionally ignored. */
@net.dodian.uber.game.netty.listener.PacketHandler(opcodes = {45, 86, 120, 202})
public final class NoOpPacketListener implements PacketListener {
    @Override
    public void handle(Client client, GamePacket packet) {
        packet.payload().skipBytes(packet.payload().readableBytes());
    }
}
