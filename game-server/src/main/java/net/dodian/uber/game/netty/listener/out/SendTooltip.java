package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;

public class SendTooltip implements OutgoingPacket {

    private final String string;
    private final int id;

    public SendTooltip(String string, int id) {
        this.string = string;
        this.id = id;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(203, MessageType.VAR_SHORT);
        message.putString(string);
        message.putShort(id, ValueType.ADD);
        client.send(message);
    }
}
