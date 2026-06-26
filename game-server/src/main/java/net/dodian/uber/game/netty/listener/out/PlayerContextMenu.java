package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;


public class PlayerContextMenu implements OutgoingPacket {

    private final int commandSlot;
    private final boolean enabled;
    private final String command;

    
    public PlayerContextMenu(int commandSlot, boolean enabled, String command) {
        this.commandSlot = commandSlot;
        this.enabled = enabled;
        this.command = command;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(104, MessageType.VAR);
        
        message.put(commandSlot, ValueType.NEGATE);
        
        message.put(enabled ? 1 : 0, ValueType.ADD);
        
        message.putString(command);
        
        client.send(message);
    }
}