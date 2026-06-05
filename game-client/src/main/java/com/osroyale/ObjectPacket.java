package com.osroyale;

final class ObjectPacket {

    private ObjectPacket() {
    }

    static void writeFirstClick(Buffer buffer, int objectX, int objectId, int objectY) {
        buffer.writeLEShortA(objectX);
        buffer.writeShort(objectId);
        buffer.writeShortA(objectY);
    }

    static void writeSecondClick(Buffer buffer, int objectId, int objectY, int objectX) {
        buffer.writeLEShortA(objectId);
        buffer.writeLEShort(objectY);
        buffer.writeShortA(objectX);
    }

    static void writeThirdClick(Buffer buffer, int objectX, int objectY, int objectId) {
        buffer.writeLEShort(objectX);
        buffer.writeShort(objectY);
        buffer.writeLEShortA(objectId);
    }

    static void writeFourthClick(Buffer buffer, int objectX, int objectId, int objectY) {
        buffer.writeLEShortA(objectX);
        buffer.writeShortA(objectId);
        buffer.writeLEShortA(objectY);
    }

    static void writeFifthClick(Buffer buffer, int objectId, int objectY, int objectX) {
        buffer.writeShortA(objectId);
        buffer.writeShortA(objectY);
        buffer.writeShort(objectX);
    }

    static void writeItemOnObject(Buffer buffer, int interfaceId, int objectId, int objectY, int itemSlot, int objectX, int itemId) {
        buffer.writeShort(interfaceId);
        buffer.writeShort(objectId);
        buffer.writeLEShortA(objectY);
        buffer.writeLEShort(itemSlot);
        buffer.writeLEShortA(objectX);
        buffer.writeShort(itemId);
    }
}
