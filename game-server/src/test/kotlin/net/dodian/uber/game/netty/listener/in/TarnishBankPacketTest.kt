package net.dodian.uber.game.netty.listener.`in`

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TarnishBankPacketTest {
    @Test
    fun `first item and placeholder actions decode three transformed shorts`() {
        val normal = Unpooled.buffer(6)
        val placeholder = Unpooled.buffer(6)
        try {
            writeShortAdd(normal, 5382)
            writeShortAdd(normal, 17)
            writeShortAdd(normal, 995)
            assertArrayEquals(intArrayOf(5382, 17, 995), RemoveItemListener.decode(normal))

            writeShortAdd(placeholder, 968)
            writeShortAdd(placeholder, 17)
            writeShortAdd(placeholder, 995)
            assertArrayEquals(intArrayOf(968, 17, 995), RemoveItemListener.decode(placeholder))
        } finally {
            normal.release()
            placeholder.release()
        }
    }

    @Test
    fun `five ten and all actions match Tarnish writers`() {
        val five = Unpooled.buffer(6)
        val ten = Unpooled.buffer(6)
        val all = Unpooled.buffer(6)
        try {
            writeLittleShortAdd(five, 5382)
            writeLittleShortAdd(five, 995)
            writeLittleShort(five, 17)
            assertArrayEquals(intArrayOf(5382, 995, 17), Bank5Listener.decode(five))

            writeLittleShort(ten, 5382)
            writeShortAdd(ten, 995)
            writeShortAdd(ten, 17)
            assertArrayEquals(intArrayOf(5382, 995, 17), Bank10Listener.decode(ten))

            writeLittleShort(all, 5382)
            writeShortAdd(all, 995)
            writeShortAdd(all, 17)
            assertArrayEquals(intArrayOf(5382, 995, 17), BankAllListener.decode(all))
        } finally {
            five.release()
            ten.release()
            all.release()
        }
    }

    @Test
    fun `bank drag packet uses seven byte Tarnish layout`() {
        val payload = Unpooled.buffer(7)
        try {
            writeLittleShortAdd(payload, 5382)
            payload.writeByte(254) // writeNegatedByte(2)
            writeLittleShortAdd(payload, 8)
            writeLittleShort(payload, 3)
            assertArrayEquals(intArrayOf(5382, 2, 8, 3), MoveItemsListener.decode(payload))
            assertEquals(0, payload.readableBytes())
        } finally {
            payload.release()
        }
    }

    private fun writeShortAdd(buffer: ByteBuf, value: Int) {
        buffer.writeByte(value ushr 8)
        buffer.writeByte(value + 128)
    }

    private fun writeLittleShort(buffer: ByteBuf, value: Int) {
        buffer.writeByte(value)
        buffer.writeByte(value ushr 8)
    }

    private fun writeLittleShortAdd(buffer: ByteBuf, value: Int) {
        buffer.writeByte(value + 128)
        buffer.writeByte(value ushr 8)
    }
}
