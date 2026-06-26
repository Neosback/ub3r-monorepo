package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class LoadPrivateMessage implements OutgoingPacket {

    private final long name;
    private final int world;

    
    public LoadPrivateMessage(long name, int world) {
        this.name = name;
        this.world = world != 0 ? world + 9 : 1;
        //System.out.println("loadpm " + name + " " + this.world);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(50, MessageType.FIXED);
        message.putLong(name); 
        message.put(world);    
        client.send(message);
    }
}