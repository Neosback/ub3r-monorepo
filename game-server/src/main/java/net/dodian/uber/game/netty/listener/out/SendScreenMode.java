package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendScreenMode implements OutgoingPacket {

    private final int width;
    private final int height;

    public SendScreenMode(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SendScreenMode(width, height).encode());
    }

}