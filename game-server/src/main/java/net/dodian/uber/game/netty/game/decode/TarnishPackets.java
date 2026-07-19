package net.dodian.uber.game.netty.game.decode;

import io.netty.buffer.ByteBuf;
import net.dodian.uber.game.netty.codec.ByteBufReader;
import net.dodian.uber.game.netty.codec.ByteOrder;
import net.dodian.uber.game.netty.codec.ValueType;

/**
 * Typed inbound packet definitions for the Tarnish 317 client.
 *
 * This is the single place where an opcode's wire format lives. Every decode
 * mirrors the exact write sequence in the Tarnish client's
 * {@code com.osroyale.Client} ({@code outgoing.writeOpcode(N)} blocks), using the
 * encoding key from {@code com.osroyale.Buffer}:
 *
 * <pre>
 *   writeShort    = BIG    + NORMAL
 *   writeShortA   = BIG    + ADD
 *   writeLEShort  = LITTLE + NORMAL
 *   writeLEShortA = LITTLE + ADD
 * </pre>
 *
 * Listeners should call these instead of hand-reading fields so a format fix
 * lands in exactly one place. Each decoder validates payload length and returns
 * {@code null} for malformed payloads — the caller drops the packet (never
 * disconnects; see PacketItemActionService for the policy rationale).
 */
public final class TarnishPackets {

    private TarnishPackets() {}

    /** Opcode 122 — first item option (wield/bury/eat). Client: LEShortA iface, ShortA slot, LEShort item. */
    public static final class ItemOption1 {
        private final int interfaceId;
        private final int slot;
        private final int itemId;

        public ItemOption1(int interfaceId, int slot, int itemId) {
            this.interfaceId = interfaceId;
            this.slot = slot;
            this.itemId = itemId;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int slot() {
            return slot;
        }

        public int itemId() {
            return itemId;
        }
        public static final int OPCODE = 122;
        public static final int SIZE = 6;

        public static ItemOption1 decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            return new ItemOption1(interfaceId, slot, itemId);
        }
    }

    /** Opcode 16 — second item option. Client: ShortA item, LEShortA slot, LEShortA iface. */
    public static final class ItemOption2 {
        private final int itemId;
        private final int slot;
        private final int interfaceId;

        public ItemOption2(int itemId, int slot, int interfaceId) {
            this.itemId = itemId;
            this.slot = slot;
            this.interfaceId = interfaceId;
        }

        public int itemId() {
            return itemId;
        }

        public int slot() {
            return slot;
        }

        public int interfaceId() {
            return interfaceId;
        }
        public static final int OPCODE = 16;
        public static final int SIZE = 6;

        public static ItemOption2 decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            return new ItemOption2(itemId, slot, interfaceId);
        }
    }

    /** Opcode 75 — third item option. Client: LEShortA iface, LEShort slot, ShortA item. */
    public static final class ItemOption3 {
        private final int interfaceId;
        private final int slot;
        private final int itemId;

        public ItemOption3(int interfaceId, int slot, int itemId) {
            this.interfaceId = interfaceId;
            this.slot = slot;
            this.itemId = itemId;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int slot() {
            return slot;
        }

        public int itemId() {
            return itemId;
        }
        public static final int OPCODE = 75;
        public static final int SIZE = 6;

        public static ItemOption3 decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new ItemOption3(interfaceId, slot, itemId);
        }
    }

    /** Opcode 41 — wield/wear item. Client: Short item, ShortA slot, ShortA iface. */
    public static final class WearItem {
        private final int itemId;
        private final int slot;
        private final int interfaceId;

        public WearItem(int itemId, int slot, int interfaceId) {
            this.itemId = itemId;
            this.slot = slot;
            this.interfaceId = interfaceId;
        }

        public int itemId() {
            return itemId;
        }

        public int slot() {
            return slot;
        }

        public int interfaceId() {
            return interfaceId;
        }
        public static final int OPCODE = 41;
        public static final int SIZE = 6;

        public static WearItem decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new WearItem(itemId, slot, interfaceId);
        }
    }

    /**
     * Opcode 53 — item on item. Client writes 6 shorts; only the two leading slots
     * carry gameplay meaning (matches Tarnish server's own decoder, which ignores
     * the trailing fields).
     */
    public static final class ItemOnItem {
        private final int usedWithSlot;
        private final int itemUsedSlot;

        public ItemOnItem(int usedWithSlot, int itemUsedSlot) {
            this.usedWithSlot = usedWithSlot;
            this.itemUsedSlot = itemUsedSlot;
        }

        public int usedWithSlot() {
            return usedWithSlot;
        }

        public int itemUsedSlot() {
            return itemUsedSlot;
        }
        public static final int OPCODE = 53;
        public static final int MIN_SIZE = 4;

        public static ItemOnItem decode(ByteBuf buf) {
            if (buf.readableBytes() < MIN_SIZE) return null;
            int usedWithSlot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int itemUsedSlot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new ItemOnItem(usedWithSlot, itemUsedSlot);
        }
    }

    /**
     * Opcode 192 — item on object. Client: Short ifaceType, LEShort objectId,
     * LEShortA y, LEShort slot, LEShortA x, Short itemId (mirrors Tarnish server's
     * UseItemPacketListener.handleItemOnObject).
     */
    public static final class ItemOnObject {
        private final int interfaceId;
        private final int objectId;
        private final int objectY;
        private final int slot;
        private final int objectX;
        private final int itemId;

        public ItemOnObject(int interfaceId, int objectId, int objectY, int slot, int objectX, int itemId) {
            this.interfaceId = interfaceId;
            this.objectId = objectId;
            this.objectY = objectY;
            this.slot = slot;
            this.objectX = objectX;
            this.itemId = itemId;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int objectId() {
            return objectId;
        }

        public int objectY() {
            return objectY;
        }

        public int slot() {
            return slot;
        }

        public int objectX() {
            return objectX;
        }

        public int itemId() {
            return itemId;
        }
        public static final int OPCODE = 192;
        public static final int SIZE = 12;

        public static ItemOnObject decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int objectId = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            int objectY = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            int objectX = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.ADD);
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            return new ItemOnObject(interfaceId, objectId, objectY, slot, objectX, itemId);
        }
    }

    /** Opcode 87 — drop item. Client: ShortA item, Short iface, ShortA slot. */
    public static final class DropItem {
        private final int itemId;
        private final int interfaceId;
        private final int slot;

        public DropItem(int itemId, int interfaceId, int slot) {
            this.itemId = itemId;
            this.interfaceId = interfaceId;
            this.slot = slot;
        }

        public int itemId() {
            return itemId;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int slot() {
            return slot;
        }
        public static final int OPCODE = 87;
        public static final int SIZE = 6;

        public static DropItem decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new DropItem(itemId, interfaceId, slot);
        }
    }

    /** Opcode 57 — use item on npc. Client: ShortA item, ShortA npcIndex, LEShort slot, ShortA iface. */
    public static final class ItemOnNpc {
        private final int itemId;
        private final int npcIndex;
        private final int slot;
        private final int interfaceId;

        public ItemOnNpc(int itemId, int npcIndex, int slot, int interfaceId) {
            this.itemId = itemId;
            this.npcIndex = npcIndex;
            this.slot = slot;
            this.interfaceId = interfaceId;
        }

        public int itemId() {
            return itemId;
        }

        public int npcIndex() {
            return npcIndex;
        }

        public int slot() {
            return slot;
        }

        public int interfaceId() {
            return interfaceId;
        }
        public static final int OPCODE = 57;
        public static final int SIZE = 8;

        public static ItemOnNpc decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int npcIndex = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new ItemOnNpc(itemId, npcIndex, slot, interfaceId);
        }
    }

    /** Opcode 14 — use item on player. Client: ShortA iface, Short playerIndex, Short item, LEShort slot. */
    public static final class ItemOnPlayer {
        private final int interfaceId;
        private final int playerIndex;
        private final int itemId;
        private final int slot;

        public ItemOnPlayer(int interfaceId, int playerIndex, int itemId, int slot) {
            this.interfaceId = interfaceId;
            this.playerIndex = playerIndex;
            this.itemId = itemId;
            this.slot = slot;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int playerIndex() {
            return playerIndex;
        }

        public int itemId() {
            return itemId;
        }

        public int slot() {
            return slot;
        }
        public static final int OPCODE = 14;
        public static final int SIZE = 8;

        public static ItemOnPlayer decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int playerIndex = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.LITTLE, ValueType.NORMAL);
            return new ItemOnPlayer(interfaceId, playerIndex, itemId, slot);
        }
    }

    /** Opcode 237 — magic on inventory item. Client: Short slot, ShortA item, Short iface, ShortA spell. */
    public static final class MagicOnItem {
        private final int slot;
        private final int itemId;
        private final int interfaceId;
        private final int spellId;

        public MagicOnItem(int slot, int itemId, int interfaceId, int spellId) {
            this.slot = slot;
            this.itemId = itemId;
            this.interfaceId = interfaceId;
            this.spellId = spellId;
        }

        public int slot() {
            return slot;
        }

        public int itemId() {
            return itemId;
        }

        public int interfaceId() {
            return interfaceId;
        }

        public int spellId() {
            return spellId;
        }
        public static final int OPCODE = 237;
        public static final int SIZE = 8;

        public static MagicOnItem decode(ByteBuf buf) {
            if (buf.readableBytes() != SIZE) return null;
            int slot = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int itemId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            int interfaceId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.NORMAL);
            int spellId = ByteBufReader.readShortUnsigned(buf, ByteOrder.BIG, ValueType.ADD);
            return new MagicOnItem(slot, itemId, interfaceId, spellId);
        }
    }
}
