package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class SendInterfaceAnimation implements OutgoingPacket {
    private final int mainFrame;
    private final int subFrame;

    
    public SendInterfaceAnimation(int mainFrame, int subFrame) {
        this.mainFrame = mainFrame;
        this.subFrame = subFrame;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(200, MessageType.FIXED);
        
        // Legacy sendFrame200: writeWordBigEndian for both values
        message.putShort(mainFrame, ByteOrder.BIG);
        message.putShort(subFrame, ByteOrder.BIG);
        
        client.send(message);
    }
}