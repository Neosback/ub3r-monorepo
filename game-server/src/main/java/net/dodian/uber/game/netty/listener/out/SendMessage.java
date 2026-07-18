package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class SendMessage implements OutgoingPacket {

    private final String message;
    private final boolean filtered;

    public SendMessage(String message) {
        this(message, false);
    }

    public SendMessage(String message, boolean filtered) {
        this.message = message;
        this.filtered = filtered;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(253, MessageType.VAR);
        message.putString(this.message);
        message.put(filtered ? 1 : 0);
        client.send(message);
    }

}
