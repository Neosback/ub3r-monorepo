package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;


public class SetGoldItems implements OutgoingPacket {

    private final int slot;
    private final int[] items;

    
    public SetGoldItems(int slot, int[] items) {
        this.slot = slot;
        this.items = items;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(53, MessageType.VAR_SHORT);

        message.putInt(slot);
        message.putShort(items.length);

        StringBuilder preview = new StringBuilder();
        for (int item : items) {
            message.putInt(1);
            message.putShort(item + 1);
            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(item).append("x1");
            }
        }

        ItemContainerTrace.log(client, "SetGoldItems", slot, items.length, preview.toString());
        client.send(message);
    }
}