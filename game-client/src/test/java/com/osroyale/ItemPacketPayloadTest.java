package com.osroyale;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ItemPacketPayloadTest {

    private static final int INTERFACE_ID = 3214;
    private static final int ITEM_ID = 995;
    private static final int SLOT = 7;

    @Test
    public void firstClickMatchesServerClickItemListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeFirstClick(buffer, INTERFACE_ID, ITEM_ID, SLOT);

        assertArrayEquals(new byte[] {0x0C, (byte) 0x8E, 0x03, (byte) 0xE3, 0, 7}, copy(buffer));
    }

    @Test
    public void secondClickMatchesServerClickItem2ListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeSecondClick(buffer, ITEM_ID, SLOT);

        assertArrayEquals(new byte[] {0x03, 0x63, (byte) 0x87, 0}, copy(buffer));
    }

    @Test
    public void thirdClickMatchesServerClickItem3ListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeThirdClick(buffer, INTERFACE_ID, SLOT, ITEM_ID);

        assertArrayEquals(new byte[] {0x0C, (byte) 0x8E, 7, 0, 0x03, 0x63}, copy(buffer));
    }

    @Test
    public void removeMatchesServerRemoveItemListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeRemove(buffer, INTERFACE_ID, SLOT, ITEM_ID);

        assertArrayEquals(new byte[] {0, 0, 0x0C, (byte) 0x8E, 0, (byte) 0x87, 0x03, 0x63}, copy(buffer));
    }

    @Test
    public void bankFiveMatchesServerBank5ListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeBank5(buffer, INTERFACE_ID, ITEM_ID, SLOT);

        assertArrayEquals(new byte[] {0, 0, 0x0C, (byte) 0x8E, 0x63, 0x03, 7, 0}, copy(buffer));
    }

    @Test
    public void bankTenMatchesServerBank10ListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeBank10(buffer, INTERFACE_ID, ITEM_ID, SLOT);

        assertArrayEquals(new byte[] {0, 0, 0x0C, (byte) 0x8E, 0x03, 0x63, 0, (byte) 0x87}, copy(buffer));
    }

    @Test
    public void bankAllMatchesServerBankAllListenerLayout() {
        Buffer buffer = Buffer.create();

        ItemPacket.writeBankAll(buffer, SLOT, INTERFACE_ID, ITEM_ID);

        assertArrayEquals(new byte[] {0, (byte) 0x87, 0, 0, 0x0C, (byte) 0x8E, 0x03, 0x63}, copy(buffer));
    }

    private static byte[] copy(Buffer buffer) {
        byte[] copy = new byte[buffer.position];
        System.arraycopy(buffer.array, 0, copy, 0, buffer.position);
        return copy;
    }
}
