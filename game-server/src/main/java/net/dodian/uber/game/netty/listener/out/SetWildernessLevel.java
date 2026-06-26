package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;


public class SetWildernessLevel implements OutgoingPacket {

    private final int level;

    
    public SetWildernessLevel(int level) {
        this.level = level;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(208, MessageType.FIXED);
        message.putInt(197); 
        client.send(message);
        
        client.send(new SendString("Level: " + level, 199));
    }
}