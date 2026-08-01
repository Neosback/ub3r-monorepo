package net.dodian.uber.game.netty.listener.out

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TarnishItemContainerEncoderTest {
    @Test
    fun `full container uses compact amounts mandatory item ids and tab header`() {
        val message = TarnishItemContainerEncoder.full(
            3214,
            intArrayOf(-1, 995, 1856, 4155),
            intArrayOf(0, 1, 254, 255),
            intArrayOf(3, 7),
        )
        try {
            val payload = message.buffer
            assertEquals(53, message.opcode)
            assertEquals(3214, payload.readInt())
            assertEquals(4, payload.readUnsignedShort())
            assertEquals(2, payload.readUnsignedShort())

            assertEquals(0, payload.readUnsignedByte())
            assertEquals(0, payload.readUnsignedShort())
            assertEquals(1, payload.readUnsignedByte())
            assertEquals(996, payload.readUnsignedShort())
            assertEquals(254, payload.readUnsignedByte())
            assertEquals(1857, payload.readUnsignedShort())
            assertEquals(255, payload.readUnsignedByte())
            assertEquals(255, payload.readInt())
            assertEquals(4156, payload.readUnsignedShort())
            assertEquals(3, payload.readInt())
            assertEquals(7, payload.readInt())
            assertEquals(0, payload.readableBytes())
        } finally {
            message.releaseAll()
        }
    }

    @Test
    fun `single slot uses unsigned shorts and compact large amount`() {
        val message = TarnishItemContainerEncoder.slot(1688, 12, 4155, 10_000)
        try {
            val payload = message.buffer
            assertEquals(34, message.opcode)
            assertEquals(1688, payload.readUnsignedShort())
            assertEquals(12, payload.readUnsignedShort())
            assertEquals(4156, payload.readUnsignedShort())
            assertEquals(255, payload.readUnsignedByte())
            assertEquals(10_000, payload.readInt())
            assertEquals(0, payload.readableBytes())
        } finally {
            message.releaseAll()
        }
    }

    @Test
    fun `legacy negative item marker is encoded as an empty slot`() {
        val full = TarnishItemContainerEncoder.full(58041, intArrayOf(-1), intArrayOf(1))
        val slot = TarnishItemContainerEncoder.slot(1688, 3, -1, 1)
        try {
            val fullPayload = full.buffer
            assertEquals(58041, fullPayload.readInt())
            assertEquals(1, fullPayload.readUnsignedShort())
            assertEquals(0, fullPayload.readUnsignedShort())
            assertEquals(0, fullPayload.readUnsignedByte())
            assertEquals(0, fullPayload.readUnsignedShort())
            assertEquals(0, fullPayload.readableBytes())

            val slotPayload = slot.buffer
            assertEquals(1688, slotPayload.readUnsignedShort())
            assertEquals(3, slotPayload.readUnsignedShort())
            assertEquals(0, slotPayload.readUnsignedShort())
            assertEquals(0, slotPayload.readUnsignedByte())
            assertEquals(0, slotPayload.readableBytes())
        } finally {
            full.releaseAll()
            slot.releaseAll()
        }
    }

    @Test
    fun `bank placeholder preserves item id with zero amount`() {
        val message = TarnishItemContainerEncoder.fullPreservingZeroAmounts(
            5382,
            intArrayOf(995),
            intArrayOf(0),
            intArrayOf(1),
        )
        try {
            val payload = message.buffer
            assertEquals(5382, payload.readInt())
            assertEquals(1, payload.readUnsignedShort())
            assertEquals(1, payload.readUnsignedShort())
            assertEquals(0, payload.readUnsignedByte())
            assertEquals(996, payload.readUnsignedShort())
            assertEquals(1, payload.readInt())
            assertEquals(0, payload.readableBytes())
        } finally {
            message.releaseAll()
        }
    }

    @Test
    fun `rejects invalid container input`() {
        assertThrows(IllegalArgumentException::class.java) {
            TarnishItemContainerEncoder.full(3214, intArrayOf(995), intArrayOf())
        }
        assertThrows(IllegalArgumentException::class.java) {
            TarnishItemContainerEncoder.slot(1688, 0, 995, -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            TarnishItemContainerEncoder.slot(1688, 65_536, 995, 1)
        }
    }
}
