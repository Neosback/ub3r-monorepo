package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class DeleteArrow implements OutgoingPacket {

    private final int itemId;
    private final int slot;
    private final int amount;

    
    public DeleteArrow(int itemId, int slot, int amount) {
        this.itemId = itemId;
        this.slot = slot;
        this.amount = amount;
    }

    @Override
    public void send(Client client) {
        int safeAmount = Math.max(0, amount);
        ByteMessage message = TarnishItemContainerEncoder.slot(1688, slot, itemId, safeAmount);

        client.send(message);
    }
}
