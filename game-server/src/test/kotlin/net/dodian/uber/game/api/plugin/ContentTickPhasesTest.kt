package net.dodian.uber.game.api.plugin

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ContentTickPhasesTest {
    @AfterEach fun cleanUp() = ContentTickPhases.clearForTests()

    @Test
    fun `handlers execute in deterministic owner order and can unregister`() {
        val calls = mutableListOf<String>()
        ContentTickPhases.register("zeta", ContentTickPhase.POST_SIMULATION) { calls += "zeta" }
        val alpha = ContentTickPhases.register("alpha", ContentTickPhase.POST_SIMULATION) { calls += "alpha" }
        ContentTickPhases.run(ContentTickPhase.POST_SIMULATION)
        alpha.close()
        ContentTickPhases.run(ContentTickPhase.POST_SIMULATION)
        assertEquals(listOf("alpha", "zeta", "zeta"), calls)
    }

    @Test
    fun `an owner cannot register the same phase twice`() {
        ContentTickPhases.register("plugin.test", ContentTickPhase.PRE_SIMULATION) {}
        assertThrows(IllegalArgumentException::class.java) {
            ContentTickPhases.register("plugin.test", ContentTickPhase.PRE_SIMULATION) {}
        }
    }
}
