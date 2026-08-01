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
        int[] itemIds = new int[items.size()];
        int[] amounts = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            PartyRoomRewardItem item = items.get(i);
            itemIds[i] = item.getId();
            amounts[i] = item.getAmount();
        }
        ByteMessage message = TarnishItemContainerEncoder.full(interfaceId, itemIds, amounts);
        client.send(message);
    }
}
