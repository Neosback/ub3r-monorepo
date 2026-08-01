package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

import java.util.Collection;


public class DuelItemsUpdate implements OutgoingPacket {

    private final int interfaceId;
    private final Collection<GameItem> items;
    private final boolean fillEmptySlots;

    
    public DuelItemsUpdate(int interfaceId, Collection<GameItem> items, boolean fillEmptySlots) {
        this.interfaceId = interfaceId;
        this.items = items;
        this.fillEmptySlots = fillEmptySlots;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.full(interfaceId, items);

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

        ItemContainerTrace.log(client, "DuelItemsUpdate", interfaceId, items.size(), preview.toString());
        client.send(message);
    }
}
