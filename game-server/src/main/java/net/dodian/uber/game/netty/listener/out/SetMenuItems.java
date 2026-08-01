package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.*;
import net.dodian.uber.game.activity.partyroom.PartyRoomRewardItem;

/**
 * Sends the shop/menu item list to interface 8847.
 * Uses Tarnish's canonical opcode-53 item-container layout.
 */
public class SetMenuItems implements OutgoingPacket {

    private final int[] items;

    public SetMenuItems(int[] items) {
        this.items = items;
    }

    @Override
    public void send(Client client) {
        int[] amounts = new int[items.length];
        java.util.Arrays.fill(amounts, 1);
        ByteMessage msg = TarnishItemContainerEncoder.full(8847, items, amounts);
        client.send(msg);
    }
}
