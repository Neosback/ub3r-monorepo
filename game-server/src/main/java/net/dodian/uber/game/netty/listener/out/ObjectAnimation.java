package net.dodian.uber.game.netty.listener.out;

import net.dodian.cache.objects.GameObjectDef;
import net.dodian.uber.game.model.Position;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class ObjectAnimation implements OutgoingPacket {

    private final GameObjectDef def;
    private final Position position;
    private final int animation;

    public ObjectAnimation(GameObjectDef object, int animation) {
        this.def = object;
        this.animation = animation;
        this.position = def.getPosition();
    }

    @Override
    public void send(Client client) {
        client.send(new SetMap(position));
        int config = (def.getType() << 2) + (def.getFace() & 3);
        client.send(new TarnishOutboundPackets.ObjectAnimation(config, animation).encode());
    }

}