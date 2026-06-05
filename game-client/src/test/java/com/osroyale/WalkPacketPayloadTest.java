package com.osroyale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class WalkPacketPayloadTest {

    @Test
    public void oneTileWalkPayloadMatchesServerWalkingListenerLayout() {
        Buffer buffer = Buffer.create();

        WalkPacket.writePayload(buffer, 3200, 3201, true);

        assertArrayEquals(new byte[] {0, (byte) 0x0C, (byte) 0x81, (byte) 0x0C, (byte) 0xFF},
                copy(buffer.array, buffer.position));
    }

    @Test
    public void routedWalkPayloadIncludesMysticStyleIntermediateDeltas() {
        Buffer buffer = Buffer.create();

        WalkPacket.writePayload(buffer,
                new int[] {10, 11, 12},
                new int[] {20, 20, 21},
                3,
                3200,
                3200,
                false);

        assertArrayEquals(new byte[] {
                0x0A, 0x0C,
                1, 0,
                2, 1,
                (byte) 0x94, 0x0C,
                0
        }, copy(buffer.array, buffer.position));
    }

    private static byte[] copy(byte[] source, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(source, 0, copy, 0, length);
        return copy;
    }
}
