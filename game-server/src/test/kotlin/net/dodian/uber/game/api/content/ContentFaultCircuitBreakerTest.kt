package net.dodian.uber.game.api.content

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContentFaultCircuitBreakerTest {
    @AfterEach
    fun reset() = ContentFaultCircuitBreaker.resetForTests()

    @Test
    fun `quarantines only the repeatedly failing binding`() {
        val bad = "skill.object:1:100"
        val healthy = "skill.object:1:101"

        repeat(3) { ContentFaultCircuitBreaker.recordFailure(bad) }

        assertFalse(ContentFaultCircuitBreaker.allows(bad))
        assertTrue(ContentFaultCircuitBreaker.allows(healthy))
        assertTrue(ContentFaultCircuitBreaker.reEnable(bad))
        assertTrue(ContentFaultCircuitBreaker.allows(bad))
    }

    @Test
    fun `records module attribution for operator diagnostics`() {
        ContentFaultCircuitBreaker.recordFailure("skill.object:1:100", "skill.mining")

        val moduleFaults = ContentFaultCircuitBreaker.failuresForModule("skill.mining")
        assertEquals(1, moduleFaults.size)
        assertEquals(1, moduleFaults.getValue("skill.object:1:100").getValue("totalFailures"))
    }
}
