package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class ReplaceObject2 implements OutgoingPacket {

    private final int newObjectId;
    private final int face;
    private final int type;

    public ReplaceObject2(int newObjectId, int face, int type) {
        this.newObjectId = newObjectId;
        this.face = face;
        this.type = type;
    }

    @Override
    public void send(Client client) {
        int config = (type << 2) + (face & 3);
        client.send(new TarnishOutboundPackets.ClearObject(config).encode());
        if (newObjectId != -1) {
            client.send(new TarnishOutboundPackets.PlaceObject(newObjectId, config).encode());
        }
    }
}