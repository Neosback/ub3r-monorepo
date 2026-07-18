package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;
import net.dodian.uber.game.activity.partyroom.PartyRoomRewardItem;


public class ShowMenuItems2 implements OutgoingPacket {

    private final int[] items;
    private final int[] amounts;

    
    public ShowMenuItems2(int[] items, int[] amounts) {
        if (items.length != amounts.length) {
            throw new IllegalArgumentException("Items and amounts arrays must be the same length");
        }
        this.items = items.clone();
        this.amounts = amounts.clone();
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.full(8847, items, amounts);
        
        client.send(message);
    }
}
