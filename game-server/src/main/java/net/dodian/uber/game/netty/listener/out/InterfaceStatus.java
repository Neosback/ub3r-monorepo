package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public class InterfaceStatus implements OutgoingPacket {

    private final int interfaceId;
    private final boolean show;

    public InterfaceStatus(int interfaceId, boolean show) {
        this.interfaceId = interfaceId;
        this.show = show;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetInterfaceConfig(show ? 0 : 1, interfaceId).encode());
    }
}