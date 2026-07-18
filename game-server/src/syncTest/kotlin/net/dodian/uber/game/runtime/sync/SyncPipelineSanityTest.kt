package net.dodian.uber.game.runtime.sync

import net.dodian.uber.game.engine.net.OutboundSessionQueue
import net.dodian.uber.game.netty.codec.ByteMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Transport-level smoke checks kept in the dedicated synchronization source set.
 */
class SyncPipelineSanityTest {

    @Test
    fun `required outbound capacity rejects without evicting an accepted packet`() {
        val queue = OutboundSessionQueue()
        repeat(1024) { assertTrue(queue.enqueue(ByteMessage.raw(1))) }
        val rejected = ByteMessage.raw(1)
        assertFalse(queue.enqueue(rejected))
        assertEquals(1024, queue.size())
        assertEquals(0, rejected.refCnt())
        queue.releaseAll()
    }
}
