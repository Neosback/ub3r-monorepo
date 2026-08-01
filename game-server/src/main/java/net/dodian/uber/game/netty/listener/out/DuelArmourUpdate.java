package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;


public class DuelArmourUpdate implements OutgoingPacket {

    private final int[] equipment;
    private final int[] equipmentN;

    
    public DuelArmourUpdate(int[] equipment, int[] equipmentN) {
        this.equipment = equipment;
        this.equipmentN = equipmentN;
    }

    @Override
    public void send(Client client) {
        for (int slot = 0; slot < equipment.length; slot++) {
            int safeAmount = equipmentN != null && slot < equipmentN.length ? equipmentN[slot] : 0;
            if (safeAmount < 0) {
                safeAmount = 0;
            }
            int itemId = equipment != null && slot < equipment.length ? equipment[slot] : -1;
            ByteMessage message = TarnishItemContainerEncoder.slot(13824, slot, itemId, safeAmount);

            client.send(message);
        }
    }
}
