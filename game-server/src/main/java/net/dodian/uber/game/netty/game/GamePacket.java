package net.dodian.uber.game.netty.game;

import io.netty.buffer.ByteBuf;

/**
 * Lightweight container for an incoming RuneScape game packet.
 */
public final class GamePacket {

    private final int opcode;
    private final int size;
    private final ByteBuf payload;

    public GamePacket(int opcode, int size, ByteBuf payload) {
        this.opcode = opcode;
        this.size = size;
        this.payload = payload;
    }

    public int opcode() {
        return opcode;
    }

    public int size() {
        return size;
    }

    public ByteBuf payload() {
        return payload;
    }
}
