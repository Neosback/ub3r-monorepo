package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.*;

public class SendString implements OutgoingPacket {

    private final String string;
    private final int lineId;

    public SendString(String string, int lineId) {
        this.string = string;
        this.lineId = lineId;
    }

    @Override
    public void send(Client client) {
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.SendString(string, lineId).encode());
    }

}