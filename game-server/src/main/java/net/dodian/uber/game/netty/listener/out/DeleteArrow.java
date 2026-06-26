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
        ByteMessage message = ByteMessage.message(34, MessageType.VAR_SHORT);

        // Match mystic client's UPDATE_SPECIFIC_ITEM layout:
        // interfaceId (uShort), slot (uByte), amount (int), id (uShort)

        message.putShort(1688);

        message.put(slot);

        // Remaining arrow amount
        int safeAmount = Math.max(0, amount);
        message.putInt(safeAmount);

        int containerId = (safeAmount > 0 && itemId > 0) ? (itemId + 1) : 0;
        message.putShort(containerId);

        client.send(message);
    }
}