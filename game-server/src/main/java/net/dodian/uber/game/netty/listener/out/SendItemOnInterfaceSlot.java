package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;

public class SendItemOnInterfaceSlot implements OutgoingPacket {

    private final int interfaceId;
    private final int itemId;
    private final int amount;
    private final int slot;

    public SendItemOnInterfaceSlot(int interfaceId, int itemId, int amount, int slot) {
        this.interfaceId = interfaceId;
        this.itemId = itemId;
        this.amount = amount;
        this.slot = slot;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.slot(interfaceId, slot, itemId, amount);
        client.send(message);
    }
}
