package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class PrivateMessageStatus implements OutgoingPacket {

    private final int status;

    
    public PrivateMessageStatus(int status) {
        this.status = status;
        //System.out.println("PM STATUS: " + status);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(221, MessageType.FIXED);
        message.put(status);
        client.send(message);
    }
}