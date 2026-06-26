package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;

public class SendScrollbar implements OutgoingPacket {

    private final int scrollbar;
    private final int size;

    public SendScrollbar(int scrollbar, int size) {
        this.scrollbar = scrollbar;
        this.size = size;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(204, MessageType.FIXED);
        message.putInt(scrollbar);
        message.putInt(size);
        client.send(message);
    }

}