package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SetInterfaceWalkable implements OutgoingPacket {

    private final int id;

    public SetInterfaceWalkable(int id) {
        this.id = id;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetInterfaceWalkable(id).encode());
    }
}
