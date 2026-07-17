package net.dodian.uber.game.model.entity.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import net.dodian.uber.game.api.plugin.skills.PendingSkillMulti
import net.dodian.uber.skills.api.SkillMultiConfig
import net.dodian.uber.skills.api.SkillMultiEntry
import net.dodian.uber.skills.api.skillRecipe

class PlayerContentRuntimeStateTest {
    @Test
    fun `skill sessions and throttles are isolated and clearable`() {
        val state = PlayerContentRuntimeState()

        state.setActiveSkillSession("fishing", 42L)
        state.setThrottleUntilCycle("content:fishing", 99L)

        assertEquals("fishing", state.getActiveSkillSessionKey())
        assertEquals(42L, state.getActiveSkillSessionStartedCycle())
        assertEquals(99L, state.getThrottleUntilCycle("content:fishing"))

        state.clearActiveSkillSession()
        state.clearThrottleUntilCycle("content:fishing")

        assertNull(state.getActiveSkillSessionKey())
        assertEquals(0L, state.getActiveSkillSessionStartedCycle())
        assertEquals(0L, state.getThrottleUntilCycle("content:fishing"))
    }

    @Test
    fun `nonpositive throttle removes an existing throttle`() {
        val state = PlayerContentRuntimeState()
        state.setThrottleUntilCycle("content:bank", 12L)
        state.setThrottleUntilCycle("content:bank", 0L)

        assertEquals(0L, state.getThrottleUntilCycle("content:bank"))
    }

    @Test
    fun `termination clears pending production menu`() {
        val state = PlayerContentRuntimeState()
        val recipe = skillRecipe("test.recipe", 2) { material(1) }
        state.setPendingSkillMulti(PendingSkillMulti(SkillMultiConfig("test.menu", entries = listOf(SkillMultiEntry(recipe)))) {})

        state.terminatePlayerTasks()

        assertNull(state.getPendingSkillMulti())
    }
}
