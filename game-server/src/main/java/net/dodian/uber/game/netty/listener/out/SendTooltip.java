package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendTooltip implements OutgoingPacket {

    private final String string;
    private final int id;

    public SendTooltip(String string, int id) {
        this.string = string;
        this.id = id;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SendTooltip(string, id).encode());
    }

}
