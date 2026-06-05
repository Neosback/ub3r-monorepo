package com.osroyale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BufferPacketFramingTest {

    @Test
    public void writesEncryptedOpcodeEncryptedTotalLengthAndPayload() {
        int[] seed = {1, 2, 3, 4};
        Buffer buffer = framedBuffer(seed);

        buffer.writeOpcode(164);
        buffer.writeLEShortA(3200);
        buffer.writeLEShort(3201);
        buffer.writeNegatedByte(1);
        buffer.endPacket();

        DecodedPacket packet = decode(buffer.array, buffer.position, seed, 0);

        assertEquals(164, packet.opcode);
        assertEquals(7, packet.totalLength);
        assertArrayEquals(new byte[] {0, (byte) 0x0C, (byte) 0x81, (byte) 0x0C, (byte) 0xFF}, packet.payload);
    }

    @Test
    public void framesMultiplePacketsInOneOutboundBuffer() {
        int[] seed = {5, 6, 7, 8};
        Buffer buffer = framedBuffer(seed);

        buffer.writeOpcode(0);
        buffer.writeOpcode(185);
        buffer.writeShort(2458);
        buffer.endPacket();

        DecodedPacket first = decode(buffer.array, buffer.position, seed, 0);
        DecodedPacket second = decode(buffer.array, buffer.position, seed, first.totalLength);

        assertEquals(0, first.opcode);
        assertEquals(2, first.totalLength);
        assertEquals(0, first.payload.length);
        assertEquals(185, second.opcode);
        assertEquals(4, second.totalLength);
        assertArrayEquals(new byte[] {9, (byte) 0x9A}, second.payload);
    }

    private static Buffer framedBuffer(int[] seed) {
        Buffer buffer = Buffer.create();
        buffer.encryption = new ISAACRandomGen(seed.clone());
        buffer.reservePacketSlots = true;
        buffer.position = 0;
        return buffer;
    }

    private static DecodedPacket decode(byte[] bytes, int limit, int[] seed, int offset) {
        ISAACRandomGen cipher = new ISAACRandomGen(seed.clone());
        for (int i = 0; i < offset; i += decodeLength(bytes, cipher, i)) {
            // Advance the cipher across earlier framed packets.
        }
        int opcode = (bytes[offset] - cipher.getNextKey()) & 0xFF;
        int length = (bytes[offset + 1] - cipher.getNextKey()) & 0xFF;
        byte[] payload = new byte[length - 2];
        System.arraycopy(bytes, offset + 2, payload, 0, payload.length);
        if (offset + length > limit) {
            throw new AssertionError("Decoded packet exceeds buffer position");
        }
        return new DecodedPacket(opcode, length, payload);
    }

    private static int decodeLength(byte[] bytes, ISAACRandomGen cipher, int offset) {
        cipher.getNextKey();
        return (bytes[offset + 1] - cipher.getNextKey()) & 0xFF;
    }

    private static final class DecodedPacket {
        private final int opcode;
        private final int totalLength;
        private final byte[] payload;

        private DecodedPacket(int opcode, int totalLength, byte[] payload) {
            this.opcode = opcode;
            this.totalLength = totalLength;
            this.payload = payload;
        }
    }
}
