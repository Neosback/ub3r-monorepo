package com.osroyale;

final class ItemPacket {

    private ItemPacket() {
    }

    static void writeFirstClick(Buffer buffer, int interfaceId, int itemId, int slot) {
        buffer.writeShort(interfaceId);
        buffer.writeShort(itemId);
        buffer.writeShort(slot);
    }

    static void writeSecondClick(Buffer buffer, int itemId, int slot) {
        buffer.writeShortA(itemId);
        buffer.writeLEShortA(slot);
    }

    static void writeThirdClick(Buffer buffer, int interfaceId, int slot, int itemId) {
        buffer.writeShort(interfaceId);
        buffer.writeLEShort(slot);
        buffer.writeShortA(itemId);
    }

    static void writeRemove(Buffer buffer, int interfaceId, int slot, int itemId) {
        buffer.writeDWord(interfaceId);
        buffer.writeShortA(slot);
        buffer.writeShortA(itemId);
    }

    static void writeBank5(Buffer buffer, int interfaceId, int itemId, int slot) {
        buffer.writeDWord(interfaceId);
        buffer.writeLEShortA(itemId);
        buffer.writeLEShort(slot);
    }

    static void writeBank10(Buffer buffer, int interfaceId, int itemId, int slot) {
        buffer.writeDWord(interfaceId);
        buffer.writeShortA(itemId);
        buffer.writeShortA(slot);
    }

    static void writeBankAll(Buffer buffer, int slot, int interfaceId, int itemId) {
        buffer.writeShortA(slot);
        buffer.writeDWord(interfaceId);
        buffer.writeShortA(itemId);
    }
}
