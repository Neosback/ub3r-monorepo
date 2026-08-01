package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;


public final class SendChatboxInterface implements OutgoingPacket {

    private final int frame;

    public SendChatboxInterface(int frame) {
        this.frame = frame;
    }

    public int frame() {
        return frame;
    }

    
    @Override
    public void send(Client client) {
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.SendChatboxInterface(frame).encode());
    }
}