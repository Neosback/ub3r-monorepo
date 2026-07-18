package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.shop.ShopManager;
import net.dodian.uber.game.model.entity.player.Client;
import net.dodian.uber.game.netty.listener.OutgoingPacket;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.MessageType;
import net.dodian.uber.game.netty.codec.ValueType;

public class ResetShop implements OutgoingPacket {

    private final int shopId;

    public ResetShop(int shopId) {
        this.shopId = shopId;
    }

    @Override
    public void send(Client client) {
        int[] itemIds = new int[ShopManager.MaxShopItems];
        int[] amounts = new int[ShopManager.MaxShopItems];
        for (int i = 0; i < ShopManager.MaxShopItems; i++) {
            amounts[i] = ShopManager.ShopItemsN[shopId][i];
            itemIds[i] = ShopManager.ShopItems[shopId][i] - 1;
        }
        ByteMessage message = TarnishItemContainerEncoder.full(3900, itemIds, amounts);
        client.send(message);
    }
}
