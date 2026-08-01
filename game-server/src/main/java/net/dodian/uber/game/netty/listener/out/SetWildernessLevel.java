package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class SetWildernessLevel implements OutgoingPacket {

    private final int level;

    public SetWildernessLevel(int level) {
        this.level = level;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetInterfaceWalkable(197).encode());
        client.send(new SendString("Level: " + level, 199));
    }
}
