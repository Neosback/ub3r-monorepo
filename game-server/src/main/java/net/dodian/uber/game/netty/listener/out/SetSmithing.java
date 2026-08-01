package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.*;

public class SetSmithing implements OutgoingPacket {

    private final int writeFrame;
    private final int[][] smithingItems;

    public SetSmithing(int writeFrame, int[][] smithingItems) {
        this.writeFrame = writeFrame;
        this.smithingItems = smithingItems;
    }

    @Override
    public void send(Client client) {
        int[] itemIds = new int[smithingItems.length];
        int[] amounts = new int[smithingItems.length];
        for (int i = 0; i < smithingItems.length; i++) {
            itemIds[i] = smithingItems[i][0];
            amounts[i] = smithingItems[i][1];
        }
        ByteMessage message = TarnishItemContainerEncoder.full(writeFrame, itemIds, amounts);
        client.send(message);
    }
}
