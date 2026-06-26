package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ByteOrder;


public class SetVarbit implements OutgoingPacket {

    private final int id;
    private final int value;

    public SetVarbit(int id, int value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public void send(Client client) {
        if (value == -1) {
            return;
        }

        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            ByteMessage msg = ByteMessage.message(87, MessageType.FIXED);
            msg.putShort(id, ByteOrder.LITTLE);
            msg.putInt(value, ByteOrder.MIDDLE);

            client.send(msg);
        } else {
            ByteMessage msg = ByteMessage.message(36, MessageType.FIXED);
            msg.putShort(id, ByteOrder.LITTLE);
            msg.put(value);

            client.send(msg);
        }
    }
}