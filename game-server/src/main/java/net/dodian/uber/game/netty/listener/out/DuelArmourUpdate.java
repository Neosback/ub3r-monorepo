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
            ByteMessage message = ByteMessage.message(34, MessageType.VAR_SHORT);

            // Match mystic client's UPDATE_SPECIFIC_ITEM layout:
            // interfaceId (uShort), slot (uByte), amount (int), id (uShort)

            message.putShort(13824);

            message.put(slot);

            int safeAmount = equipmentN != null && slot < equipmentN.length ? equipmentN[slot] : 0;
            if (safeAmount < 0) {
                safeAmount = 0;
            }
            message.putInt(safeAmount);

            int itemId = equipment != null && slot < equipment.length ? equipment[slot] : -1;
            int containerId = (safeAmount > 0 && itemId > 0) ? (itemId + 1) : 0;
            message.putShort(containerId);

            client.send(message);
        }
    }
}