package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;

public final class SendExpCounterSetting implements OutgoingPacket {
    private final int type;
    private final int modification;

    public SendExpCounterSetting(int type, int modification) {
        this.type = type;
        this.modification = modification;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(103);
        message.putInt(type);
        message.putInt(modification);
        client.send(message);
    }
}