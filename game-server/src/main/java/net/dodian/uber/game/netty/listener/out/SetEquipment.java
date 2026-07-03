package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.ui.combat.CombatStyleService;
import net.dodian.uber.game.engine.systems.combat.CombatSpecialService;


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
        ByteMessage message = ByteMessage.message(34, MessageType.VAR_SHORT);
        message.putShort(1688);           
        message.put(targetSlot);          

        // Client expects: amount (int), then item id (short)
        message.putInt(amount);            // stack size

        int itemId = wearId > 0 ? wearId + 1 : 0; // container value (id+1 or 0)
        message.putShort(itemId, ByteOrder.BIG);
        
        client.send(message);
        
        // Additional equipment-related updates
        if (targetSlot == 3) { // 3 is the weapon slot
            client.CheckGear();
            CombatStyleService.refreshWeaponStyleUi(client);
            CombatSpecialService.onWeaponEquip(client);
            client.requestWeaponAnims();
        }
        
        client.GetBonus(true);
        client.getUpdateFlags().setRequired(net.dodian.uber.game.model.entity.UpdateFlag.APPEARANCE, true);
    }
}