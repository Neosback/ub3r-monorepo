package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SetScrollPosition implements OutgoingPacket {

    private final int id;

    public SetScrollPosition(int id) {
        this.id = id;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetScrollPosition(id).encode());
    }
}