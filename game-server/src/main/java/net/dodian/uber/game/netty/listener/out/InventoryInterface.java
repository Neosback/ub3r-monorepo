package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

public final class InventoryInterface implements OutgoingPacket {

    private final int interfaceId;
    private final int inventoryId;

    public InventoryInterface(int interfaceId, int inventoryId) {
        this.interfaceId = interfaceId;
        this.inventoryId = inventoryId;
    }

    public int interfaceId() {
        return interfaceId;
    }

    public int inventoryId() {
        return inventoryId;
    }

    @Override
    public void send(Client client) {
        client.send(new TarnishOutboundPackets.SetInventoryInterface(interfaceId, inventoryId).encode());
    }

}
