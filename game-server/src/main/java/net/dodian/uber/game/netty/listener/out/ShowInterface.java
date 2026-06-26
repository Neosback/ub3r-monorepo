package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;


public final class ShowInterface implements OutgoingPacket {

    private final int interfaceId;

    public ShowInterface(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int interfaceId() {
        return interfaceId;
    }

    @Override
    public void send(Client client) {
        //System.out.println("Show interface: " + interfaceId);
        ByteMessage message = ByteMessage.message(97);
        message.putShort(interfaceId);
        client.send(message);
    }
}