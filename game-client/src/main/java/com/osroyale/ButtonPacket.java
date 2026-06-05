package com.osroyale;

public final class ButtonPacket {

    private ButtonPacket() {
    }

    public static void writeClick(Buffer buffer, int buttonId) {
        buffer.writeDWord(buttonId);
    }

    public static void writeAction(Buffer buffer, int buttonId, int actionIndex) {
        buffer.writeDWord(buttonId);
        buffer.writeByte(actionIndex);
    }
}
