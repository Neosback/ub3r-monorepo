package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SendFrame27 implements OutgoingPacket {

    private final String title;
    private final int inputLength;

    public SendFrame27() {
        this("Enter amount:", 10);
    }

    public SendFrame27(String title) {
        this(title, 10);
    }

    public SendFrame27(String title, int inputLength) {
        this.title = title != null ? title : "Enter amount:";
        this.inputLength = inputLength;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SendFrame27(title, inputLength).encode());
    }

}
