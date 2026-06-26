package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

import net.dodian.uber.game.netty.codec.ByteOrder;


public class InterfaceStatus implements OutgoingPacket {

    private final int interfaceId;
    private final boolean show;

    
    public InterfaceStatus(int interfaceId, boolean show) {
        this.interfaceId = interfaceId;
        this.show = show;
    }

    @Override
    public void send(Client client) {
        ByteMessage msg = ByteMessage.message(171);
        // Match old client's expected format: put(show ? 0 : 1) followed by putShort(interfaceId)
        msg.put(show ? 0 : 1);
        msg.putShort(interfaceId);
        client.send(msg);
    }
}