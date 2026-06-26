package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;


public class SendInterfaceModel implements OutgoingPacket {
    private final int mainFrame;
    private final int subFrame;
    private final int subFrame2;

    
    public SendInterfaceModel(int mainFrame, int subFrame, int subFrame2) {
        this.mainFrame = mainFrame;
        this.subFrame = subFrame;
        this.subFrame2 = subFrame2;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(246, MessageType.FIXED);
        
        message.putShort(mainFrame, ByteOrder.LITTLE);
        message.putShort(subFrame);
        message.putShort(subFrame2);
        
        client.send(message);
    }
}