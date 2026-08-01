package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class LoadPrivateMessage implements OutgoingPacket {

    private final long name;
    private final int world;

    public LoadPrivateMessage(long name, int world) {
        this.name = name;
        this.world = world != 0 ? world + 9 : 0;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.LoadPrivateMessage(name, world).encode());
    }
}
