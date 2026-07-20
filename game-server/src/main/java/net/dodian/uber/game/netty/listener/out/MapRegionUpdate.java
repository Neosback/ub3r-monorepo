package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public final class MapRegionUpdate implements OutgoingPacket {

    private final int mapRegionX;
    private final int mapRegionY;

    public MapRegionUpdate(int mapRegionX, int mapRegionY) {
        this.mapRegionX = mapRegionX;
        this.mapRegionY = mapRegionY;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.MapRegionUpdate(mapRegionX, mapRegionY).encode());
    }
}