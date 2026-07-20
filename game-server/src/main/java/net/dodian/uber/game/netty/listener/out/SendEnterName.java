package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendEnterName implements OutgoingPacket {

    private final String title;

    public SendEnterName(String title) {
        this.title = title;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SendEnterName(title).encode());
    }

}