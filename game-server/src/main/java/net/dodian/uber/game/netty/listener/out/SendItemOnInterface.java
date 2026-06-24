package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ByteOrder;

public class SendItemOnInterface implements OutgoingPacket {

    private final int interfaceId;
    private final int[] itemIds;
    private final int[] amounts;

    public SendItemOnInterface(int interfaceId, int[] itemIds, int[] amounts) {
        this.interfaceId = interfaceId;
        this.itemIds = itemIds;
        this.amounts = amounts;
    }

    public SendItemOnInterface(int interfaceId) {
        this(interfaceId, new int[0], new int[0]);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(53, MessageType.VAR_SHORT);
        message.putInt(interfaceId);
        message.putShort(itemIds.length);
        for (int i = 0; i < itemIds.length; i++) {
            int itemId = itemIds[i];
            int amount = amounts[i];
            message.putInt(amount);
            if (amount != 0) {
                int containerId = itemId >= 0 ? itemId + 1 : 0;
                message.putShort(containerId, ByteOrder.BIG);
            }
        }
        client.send(message);
    }
}
