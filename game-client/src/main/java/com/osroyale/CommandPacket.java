package com.osroyale;

final class CommandPacket {

    private CommandPacket() {
    }

    static void write(Buffer buffer, String command) {
        buffer.writeString(command);
    }
}
