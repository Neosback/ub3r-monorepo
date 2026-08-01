package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

import java.util.ArrayList;
import java.util.List;


public class ViewOtherPlayerBank implements OutgoingPacket {

    private final int interfaceId;
    private final List<GameItem> bankItems;

    
    public ViewOtherPlayerBank(int interfaceId, List<GameItem> bankItems) {
        this.interfaceId = interfaceId;
        this.bankItems = new ArrayList<>(bankItems);
    }

    @Override
    public void send(Client client) {
        ByteMessage message = TarnishItemContainerEncoder.full(interfaceId, bankItems);

        StringBuilder preview = new StringBuilder();
        for (GameItem item : bankItems) {
            int amount = item.getAmount();

            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(item.getId()).append('x').append(amount);
            }
        }

        ItemContainerTrace.log(client, "ViewOtherPlayerBank", interfaceId, bankItems.size(), preview.toString());
        client.send(message);
    }
}
