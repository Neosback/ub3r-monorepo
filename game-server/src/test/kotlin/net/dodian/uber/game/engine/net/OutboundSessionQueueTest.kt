package net.dodian.uber.game.engine.net

import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OutboundSessionQueueTest {
    @Test
    fun `enqueue reports rejection and consumes rejected message`() {
        val queue = OutboundSessionQueue()
        repeat(1024) {
            assertTrue(queue.enqueue(ByteMessage.raw(1)))
        }
        val rejected = ByteMessage.raw(1)
        assertFalse(queue.enqueue(rejected))
        assertEquals(0, rejected.refCnt())
        queue.releaseAll()
    }
}
