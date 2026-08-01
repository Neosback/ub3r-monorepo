package net.dodian.uber.game.netty.listener.`in`

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WalkingListenerTest {
    @Test
    fun `decodes Tarnish little endian transformed destination`() {
        val payload = Unpooled.buffer(5)
        try {
            writeLittleShort(payload, 2607)
            writeLittleShortAdd(payload, 3108)
            payload.writeByte(255) // writeNegatedByte(1)

            val request = WalkingListener.decodeTarnishDestination(164, payload)

            assertEquals(164, request.opcode)
            assertEquals(2607, request.firstStepXAbs)
            assertEquals(3108, request.firstStepYAbs)
            assertTrue(request.running)
            assertArrayEquals(intArrayOf(0), request.deltasX)
            assertArrayEquals(intArrayOf(0), request.deltasY)
            assertEquals(0, payload.readableBytes())
        } finally {
            payload.release()
        }
    }

    @Test
    fun `decodes non-running Tarnish walk`() {
        val payload = Unpooled.buffer(5)
        try {
            writeLittleShort(payload, 3200)
            writeLittleShortAdd(payload, 3201)
            payload.writeByte(0)

            val request = WalkingListener.decodeTarnishDestination(248, payload)
            assertFalse(request.running)
            assertEquals(3200, request.firstStepXAbs)
            assertEquals(3201, request.firstStepYAbs)
        } finally {
            payload.release()
        }
    }

    private fun writeLittleShort(buffer: io.netty.buffer.ByteBuf, value: Int) {
        buffer.writeByte(value)
        buffer.writeByte(value ushr 8)
    }

    private fun writeLittleShortAdd(buffer: io.netty.buffer.ByteBuf, value: Int) {
        buffer.writeByte(value + 128)
        buffer.writeByte(value ushr 8)
    }
}
