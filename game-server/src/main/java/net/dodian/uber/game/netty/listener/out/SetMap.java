package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ValueType;

public class SetMap implements OutgoingPacket {
    private final Position pos;

    public SetMap(Position pos) {
        this.pos = pos;
    }

    @Override
    public void send(Client client) {
        int localX = pos.getX() - (client.mapRegionX * 8);
        int localY = pos.getY() - (client.mapRegionY * 8);
        client.send(new net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets.SetMap(localX, localY).encode());
    }
}