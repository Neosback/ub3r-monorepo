package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SetChatOptions implements OutgoingPacket {

    private final int publicChat;
    private final int privateChat;
    private final int tradeBlock;

    public SetChatOptions(int publicChat, int privateChat, int tradeBlock) {
        this.publicChat = publicChat;
        this.privateChat = privateChat;
        this.tradeBlock = tradeBlock;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetChatOptions(publicChat, privateChat, tradeBlock).encode());
    }
}
