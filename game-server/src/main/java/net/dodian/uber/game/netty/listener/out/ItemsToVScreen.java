package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

import java.util.Collection;

/**
 * Sends items to the duel victory screen container (interface 6822).
 *
 * Uses Tarnish's canonical opcode-53 item-container layout.
 */
public class ItemsToVScreen implements OutgoingPacket {

    private final Collection<GameItem> items;

    
    public ItemsToVScreen(Collection<GameItem> items) {
        this.items = items;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.full(6822, items);

        StringBuilder preview = new StringBuilder();
        for (GameItem item : items) {
            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(item.getId()).append('x').append(item.getAmount());
            }
        }

        ItemContainerTrace.log(client, "ItemsToVScreen", 6822, items.size(), preview.toString());
        client.send(message);
    }
}
