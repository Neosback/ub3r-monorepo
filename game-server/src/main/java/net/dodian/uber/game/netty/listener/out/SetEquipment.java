package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;


public class SetEquipment implements OutgoingPacket {

    private final int wearId;
    private final int amount;
    private final int targetSlot;

    
    public SetEquipment(int wearId, int amount, int targetSlot) {
        this.wearId = wearId;
        this.amount = amount;
        this.targetSlot = targetSlot;
        
        //System.out.println("SetEquipment: Slot=" + targetSlot + ", ItemID=" + wearId + ", Amount=" + amount);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.slot(1688, targetSlot, wearId, amount);
        
        client.send(message);
    }
}
