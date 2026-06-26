package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;


public class SetScrollPosition implements OutgoingPacket {

    private final int id;

    
    public SetScrollPosition(int id) {
        this.id = id;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(79, MessageType.FIXED);
        
        // Match old behavior: writeWordBigEndian followed by writeWordA(0)
        message.putShort(id, ByteOrder.BIG);
        message.putShort(0, ValueType.NORMAL);

        //System.out.println("Sending SendQuestSomething packet with ID: " + id);
        client.send(message);
    }
}