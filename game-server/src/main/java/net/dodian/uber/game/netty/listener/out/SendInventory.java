package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.*;

import java.util.List;

/**
 * Sends an inventory (or any item container) update to the client.
 * <p>
 * Opcode: 53 (variable-short length)
 * Uses Tarnish's canonical opcode-53 item-container layout.
 */
public class SendInventory implements OutgoingPacket {

    private final int interfaceId;
    private final List<GameItem> items;

    public SendInventory(int interfaceId, List<GameItem> items) {
        this.interfaceId = interfaceId;
        this.items = items;
    }

    @Override
    public void send(Client client) {
        ByteMessage msg = TarnishItemContainerEncoder.full(interfaceId, items);
        StringBuilder preview = new StringBuilder();
        for (GameItem item : items) {
            int amount = item.getAmount();
            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(item.getId()).append('x').append(amount);
            }
        }
        ItemContainerTrace.log(client, "SendInventory", interfaceId, items.size(), preview.toString());
        client.send(msg);
    }
}
