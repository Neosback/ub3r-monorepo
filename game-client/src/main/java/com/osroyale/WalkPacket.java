package com.osroyale;

final class WalkPacket {

    private WalkPacket() {
    }

    static void writePayload(Buffer buffer, int absoluteX, int absoluteY, boolean running) {
        buffer.writeLEShortA(absoluteX);
        buffer.writeLEShort(absoluteY);
        buffer.writeNegatedByte(running ? 1 : 0);
    }

    static void writePayload(Buffer buffer, int[] localX, int[] localY, int stepCount, int baseX, int baseY, boolean running) {
        if (localX == null || localY == null || stepCount <= 0 || stepCount > localX.length || stepCount > localY.length) {
            throw new IllegalArgumentException("Invalid walk path");
        }
        int firstX = localX[0];
        int firstY = localY[0];
        buffer.writeLEShortA(firstX + baseX);
        for (int index = 1; index < stepCount; index++) {
            buffer.writeByte(localX[index] - firstX);
            buffer.writeByte(localY[index] - firstY);
        }
        buffer.writeLEShort(firstY + baseY);
        buffer.writeNegatedByte(running ? 1 : 0);
    }
}
