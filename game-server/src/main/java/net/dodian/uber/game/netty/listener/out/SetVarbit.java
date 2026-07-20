package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

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
            client.send(new TarnishOutboundPackets.SetVarbitInt(id, value).encode());
        } else {
            client.send(new TarnishOutboundPackets.SetVarbitByte(id, value).encode());
        }
    }
}