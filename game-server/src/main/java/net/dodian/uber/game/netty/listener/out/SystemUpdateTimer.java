package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public final class SystemUpdateTimer implements OutgoingPacket {

    private final int clientTicks;

    public SystemUpdateTimer(int clientTicks) {
        this.clientTicks = clientTicks;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SystemUpdateTimer(clientTicks).encode());
    }
}