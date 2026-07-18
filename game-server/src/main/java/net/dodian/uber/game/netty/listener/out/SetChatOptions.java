package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class SetChatOptions implements OutgoingPacket {

    private final int publicChat;
    private final int privateChat;
    private final int tradeBlock;

    
    public SetChatOptions(int publicChat, int privateChat, int tradeBlock) {
        this.publicChat = publicChat;
        this.privateChat = privateChat;
        this.tradeBlock = tradeBlock;
        
        //System.out.println("SetChatOptions: publicChat=" + publicChat + ", privateChat=" + privateChat + ", tradeBlock=" + tradeBlock);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(206, MessageType.FIXED);
        message.put(publicChat);
        message.put(privateChat);
        message.put(0); // clan chat mode
        message.put(tradeBlock);
        
        client.send(message);
    }
}
