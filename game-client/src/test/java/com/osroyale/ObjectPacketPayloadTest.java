package com.osroyale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ObjectPacketPayloadTest {

    private static final int OBJECT_ID = 17123;
    private static final int OBJECT_X = 2614;
    private static final int OBJECT_Y = 3106;
    private static final int INTERFACE_ID = 3214;
    private static final int ITEM_SLOT = 7;
    private static final int ITEM_ID = 995;

    @Test
    public void firstClickMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeFirstClick(buffer, OBJECT_X, OBJECT_ID, OBJECT_Y);

        assertArrayEquals(new byte[] {(byte) 0xB6, 0x0A, 0x42, (byte) 0xE3, 0x0C, (byte) 0xA2}, copy(buffer));
    }

    @Test
    public void secondClickMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeSecondClick(buffer, OBJECT_ID, OBJECT_Y, OBJECT_X);

        assertArrayEquals(new byte[] {0x63, 0x42, 0x22, 0x0C, 0x0A, (byte) 0xB6}, copy(buffer));
    }

    @Test
    public void thirdClickMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeThirdClick(buffer, OBJECT_X, OBJECT_Y, OBJECT_ID);

        assertArrayEquals(new byte[] {0x36, 0x0A, 0x0C, 0x22, 0x63, 0x42}, copy(buffer));
    }

    @Test
    public void fourthClickMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeFourthClick(buffer, OBJECT_X, OBJECT_ID, OBJECT_Y);

        assertArrayEquals(new byte[] {(byte) 0xB6, 0x0A, 0x42, 0x63, (byte) 0xA2, 0x0C}, copy(buffer));
    }

    @Test
    public void fifthClickMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeFifthClick(buffer, OBJECT_ID, OBJECT_Y, OBJECT_X);

        assertArrayEquals(new byte[] {0x42, 0x63, 0x0C, (byte) 0xA2, 0x0A, 0x36}, copy(buffer));
    }

    @Test
    public void itemOnObjectMatchesServerObjectListenerLayout() {
        Buffer buffer = Buffer.create();

        ObjectPacket.writeItemOnObject(buffer, INTERFACE_ID, OBJECT_ID, OBJECT_Y, ITEM_SLOT, OBJECT_X, ITEM_ID);

        assertArrayEquals(new byte[] {
                0x0C, (byte) 0x8E,
                0x42, (byte) 0xE3,
                (byte) 0xA2, 0x0C,
                0x07, 0x00,
                (byte) 0xB6, 0x0A,
                0x03, (byte) 0xE3
        }, copy(buffer));
    }

    private static byte[] copy(Buffer buffer) {
        byte[] copy = new byte[buffer.position];
        System.arraycopy(buffer.array, 0, copy, 0, buffer.position);
        return copy;
    }
}
