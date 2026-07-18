package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;

public class ResetItems implements OutgoingPacket {

    private final int writeFrame;

    public ResetItems(int writeFrame) {
        this.writeFrame = writeFrame;
    }

    @Override
    public void send(Client client) {

        int[] itemIds = new int[client.playerItems.length];
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < client.playerItems.length; i++) {
            itemIds[i] = client.playerItems[i] - 1;
            int amount = client.playerItemsN[i];
            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(client.playerItems[i] - 1).append('x').append(amount);
            }
        }
        ByteMessage message = TarnishItemContainerEncoder.full(writeFrame, itemIds, client.playerItemsN);
        ItemContainerTrace.log(client, "ResetItems", writeFrame, client.playerItems.length, preview.toString());
        client.send(message);
    }
}
