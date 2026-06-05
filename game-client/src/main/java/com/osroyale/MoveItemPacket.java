package com.osroyale;

final class MoveItemPacket {

    private MoveItemPacket() {
    }

    static void writeSwitch(Buffer buffer, int interfaceId, int mode, int fromSlot, int toSlot) {
        buffer.writeDWord(interfaceId);
        buffer.writeNegatedByte(mode);
        buffer.writeLEShortA(fromSlot);
        buffer.writeLEShort(toSlot);
    }
}
