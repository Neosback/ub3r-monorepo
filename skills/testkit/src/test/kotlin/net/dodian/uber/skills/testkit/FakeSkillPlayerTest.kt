package net.dodian.uber.skills.testkit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class FakeSkillPlayerTest {
    @Test
    fun `failed transaction leaves inventory unchanged and does not refresh`() {
        val player = FakeSkillPlayer(mapOf(100 to 1))
        val committed = player.inventory.transaction { remove(100, 2); add(200) }
        assertFalse(committed)
        assertEquals(1, player.amount(100))
        assertEquals(0, player.amount(200))
        assertEquals(0, player.refreshCount)
    }
}
