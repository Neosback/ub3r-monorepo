package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.activity.partyroom.PartyRoomRewardItem;

import java.util.List;


public class PartyItemsDisplay implements OutgoingPacket {

    private final int interfaceId;
    private final List<PartyRoomRewardItem> items;

    
    public PartyItemsDisplay(int interfaceId, List<PartyRoomRewardItem> items) {
        this.interfaceId = interfaceId;
        this.items = items;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(53, MessageType.VAR_SHORT);
        message.putInt(interfaceId);
        message.putShort(items.size());
        // Write each item
        for (PartyRoomRewardItem item : items) {
            int amount = item.getAmount();
            message.putInt(amount);

            if (amount != 0) {
                int itemId = item.getId() + 1; // container value (id + 1)
                message.putShort(itemId);
            }
        }
        client.send(message);
    }
}