package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.listener.OutgoingPacket;

import java.util.Collection;


public class DuelConfirmItems implements OutgoingPacket {

    private final Collection<GameItem> ownItems;
    private final Collection<GameItem> otherItems;
    private final boolean isForOwnItems;

    
    public static DuelConfirmItems forOwnItems(Collection<GameItem> ownItems, Collection<GameItem> otherItems) {
        return new DuelConfirmItems(ownItems, otherItems, true);
    }

    
    public static DuelConfirmItems forOtherItems(Collection<GameItem> ownItems, Collection<GameItem> otherItems) {
        return new DuelConfirmItems(ownItems, otherItems, false);
    }

    private DuelConfirmItems(Collection<GameItem> ownItems, Collection<GameItem> otherItems, boolean isForOwnItems) {
        this.ownItems = ownItems;
        this.otherItems = otherItems;
        this.isForOwnItems = isForOwnItems;
    }

    @Override
    public void send(Client client) {
        ByteMessage message = ByteMessage.message(53, MessageType.VAR_SHORT);

        int interfaceId;
        Collection<GameItem> itemsToSend;

        if (isForOwnItems) {
            interfaceId = otherItems.size() >= 14 ? 6509 : 6507;
            itemsToSend = ownItems;
        } else {
            interfaceId = otherItems.size() >= 14 ? 6508 : 6502;
            itemsToSend = otherItems;
        }

        message.putInt(interfaceId);
        message.putShort(itemsToSend.size());

        StringBuilder preview = new StringBuilder();
        for (GameItem item : itemsToSend) {
            int amount = item.getAmount();
            message.putInt(amount);
            if (amount != 0) {
                message.putShort(item.getId() + 1);
            }
            if (preview.length() < 120) {
                if (preview.length() > 0) {
                    preview.append(", ");
                }
                preview.append(item.getId()).append('x').append(amount);
            }
        }

        ItemContainerTrace.log(client, "DuelConfirmItems", interfaceId, itemsToSend.size(), preview.toString());
        client.send(message);
    }
}