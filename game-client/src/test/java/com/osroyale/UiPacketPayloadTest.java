package com.osroyale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class UiPacketPayloadTest {

    @Test
    public void commandMatchesMysticStringOnlyLayout() {
        Buffer buffer = Buffer.create();

        CommandPacket.write(buffer, "tele 3200 3200");

        assertArrayEquals(new byte[] {'t', 'e', 'l', 'e', ' ', '3', '2', '0', '0', ' ', '3', '2', '0', '0', 10}, copy(buffer));
    }

    @Test
    public void buttonClickUsesFourByteButtonId() {
        Buffer buffer = Buffer.create();

        ButtonPacket.writeClick(buffer, 5001);

        assertArrayEquals(new byte[] {0, 0, 0x13, (byte) 0x89}, copy(buffer));
    }

    @Test
    public void buttonActionUsesFourByteButtonIdPlusActionIndex() {
        Buffer buffer = Buffer.create();

        ButtonPacket.writeAction(buffer, 112, 3);

        assertArrayEquals(new byte[] {0, 0, 0, 0x70, 3}, copy(buffer));
    }

    @Test
    public void moveItemsMatchesServerMoveItemsListenerLayout() {
        Buffer buffer = Buffer.create();

        MoveItemPacket.writeSwitch(buffer, 3214, 2, 7, 5);

        assertArrayEquals(new byte[] {0, 0, 0x0C, (byte) 0x8E, (byte) 0xFE, (byte) 0x87, 0, 5, 0}, copy(buffer));
    }

    private static byte[] copy(Buffer buffer) {
        byte[] copy = new byte[buffer.position];
        System.arraycopy(buffer.array, 0, copy, 0, buffer.position);
        return copy;
    }
}
