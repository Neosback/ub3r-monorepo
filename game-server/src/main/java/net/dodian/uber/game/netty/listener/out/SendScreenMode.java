package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;

public class SendScreenMode implements OutgoingPacket {

    private final int width;
    private final int height;

    public SendScreenMode(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(108, MessageType.FIXED);
        message.putShort(width, ByteOrder.LITTLE, ValueType.ADD);
        message.putInt(height);
        client.send(message);
    }
}