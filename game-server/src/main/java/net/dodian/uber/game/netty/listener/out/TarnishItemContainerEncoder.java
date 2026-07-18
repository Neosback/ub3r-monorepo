package net.dodian.uber.game.netty.listener.out;

import net.dodian.uber.game.model.item.GameItem;
import net.dodian.uber.game.netty.codec.ByteMessage;
import net.dodian.uber.game.netty.codec.MessageType;

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

        ByteMessage message = ByteMessage.message(53, MessageType.VAR_SHORT);
        message.putInt(interfaceId);
        message.putShort(itemIds.length);
        message.putShort(tabAmounts == null ? 0 : tabAmounts.length);
        for (int slot = 0; slot < itemIds.length; slot++) {
            writeItem(message, itemIds[slot], amounts[slot], preserveZeroAmounts);
        }
        if (tabAmounts != null) {
            for (int amount : tabAmounts) {
                if (amount < 0) throw new IllegalArgumentException("Negative bank-tab amount: " + amount);
                message.putInt(amount);
            }
        }
        validatePayloadSize(message);
        return message;
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
        ByteMessage message = ByteMessage.message(34, MessageType.VAR_SHORT);
        message.putShort(interfaceId);
        message.putShort(slot);
        validateItem(itemId, amount);
        boolean empty = itemId < 0 || amount == 0;
        message.putShort(empty ? 0 : itemId + 1);
        writeAmount(message, empty ? 0 : amount);
        validatePayloadSize(message);
        return message;
    }

    private static void writeItem(ByteMessage message, int itemId, int amount, boolean preserveZeroAmounts) {
        validateItem(itemId, amount);
        // Legacy interfaces commonly use -1 with a placeholder amount of one.
        // Tarnish represents every empty slot as item 0 with amount 0.
        if (itemId < 0 || (amount == 0 && !preserveZeroAmounts)) {
            message.put(0);
            message.putShort(0);
            return;
        }
        writeAmount(message, amount);
        message.putShort(itemId + 1);
    }

    private static void writeAmount(ByteMessage message, int amount) {
        if (amount > 254) {
            message.put(255);
            message.putInt(amount);
        } else {
            message.put(amount);
        }
    }

    private static void validateItem(int itemId, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative item amount: " + amount);
        if (itemId >= 0xffff) {
            throw new IllegalArgumentException("Item ID cannot be encoded for Tarnish: " + itemId);
        }
    }

    private static void validatePayloadSize(ByteMessage message) {
        if (message.getBuffer().writerIndex() > 0xffff) {
            message.releaseAll();
            throw new IllegalArgumentException("Tarnish item-container payload exceeds variable-short limit");
        }
    }

    private TarnishItemContainerEncoder() {}
}
