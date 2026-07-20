package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendScrollbar implements OutgoingPacket {

    private final int scrollbar;
    private final int size;

    public SendScrollbar(int scrollbar, int size) {
        this.scrollbar = scrollbar;
        this.size = size;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SendScrollbar(scrollbar, size).encode());
    }

}