package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

/**
 * Clears an item container interface using Tarnish's opcode-53 layout.
 */
public class ClearItemContainer implements OutgoingPacket {

    private final int interfaceId;
    private final int slotCount;

    
    public ClearItemContainer(int interfaceId, int slotCount) {
        this.interfaceId = interfaceId;
        this.slotCount = slotCount;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.full(interfaceId, new int[slotCount], new int[slotCount]);

        ItemContainerTrace.log(client, "ClearItemContainer", interfaceId, slotCount, "all-zero");
        client.send(message);
    }
}
