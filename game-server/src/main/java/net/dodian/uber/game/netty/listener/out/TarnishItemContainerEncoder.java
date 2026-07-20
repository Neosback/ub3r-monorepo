package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.game.encode.TarnishOutboundPackets;

import java.util.Collection;

/** Canonical item-container wire encoding consumed by the immutable Tarnish client. */
public final class TarnishItemContainerEncoder {

    public static ByteMessage full(int interfaceId, int[] itemIds, int[] amounts) {
        return full(interfaceId, itemIds, amounts, null);
    }

    public static ByteMessage full(int interfaceId, int[] itemIds, int[] amounts, int[] tabAmounts) {
        return full(interfaceId, itemIds, amounts, tabAmounts, false);
    }

    /** Bank containers use an item id with amount zero for a visible Tarnish placeholder. */
    public static ByteMessage fullPreservingZeroAmounts(int interfaceId, int[] itemIds, int[] amounts, int[] tabAmounts) {
        return full(interfaceId, itemIds, amounts, tabAmounts, true);
    }

    private static ByteMessage full(int interfaceId, int[] itemIds, int[] amounts, int[] tabAmounts, boolean preserveZeroAmounts) {
        if (itemIds == null || amounts == null || itemIds.length != amounts.length) {
            throw new IllegalArgumentException("Item IDs and amounts must be non-null and have equal lengths");
        }
        if (itemIds.length > 0xffff || (tabAmounts != null && tabAmounts.length > 0xffff)) {
            throw new IllegalArgumentException("Tarnish item-container count exceeds unsigned short");
        }
        return new TarnishOutboundPackets.UpdateItemContainer(interfaceId, itemIds, amounts, tabAmounts, preserveZeroAmounts).encode();
    }

    public static ByteMessage full(int interfaceId, Collection<GameItem> items) {
        if (items == null) throw new IllegalArgumentException("Items cannot be null");
        int[] itemIds = new int[items.size()];
        int[] amounts = new int[items.size()];
        int slot = 0;
        for (GameItem item : items) {
            itemIds[slot] = item == null ? -1 : item.getId();
            amounts[slot] = item == null ? 0 : item.getAmount();
            slot++;
        }
        return full(interfaceId, itemIds, amounts);
    }

    public static ByteMessage slot(int interfaceId, int slot, int itemId, int amount) {
        if (interfaceId < 0 || interfaceId > 0xffff || slot < 0 || slot > 0xffff) {
            throw new IllegalArgumentException("Opcode 34 interface and slot must fit unsigned shorts");
        }
        return new TarnishOutboundPackets.UpdateItemSlot(interfaceId, slot, itemId, amount).encode();
    }

    private TarnishItemContainerEncoder() {}
}
