package net.dodian.uber.game.skill.mining

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MiningDataTest {
    @Test
    fun `yanille mine rock 11390 routes to iron mining`() {
        val rock = MiningData.rockByObjectId[11390]

        assertEquals("Iron", rock?.name)
        assertEquals(440, rock?.oreItemId)
        assertTrue(11390 in MiningData.allRockObjectIds)
    }
}
