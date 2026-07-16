package net.dodian.uber.skills.runtime

import net.dodian.uber.skills.api.skillRecipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SkillRecipePlannerTest {
    @Test
    fun `limits production by the scarcest material`() {
        val recipe = skillRecipe(100) { material(1, 2); material(2, 3) }
        assertEquals(2, SkillRecipePlanner.maxCraftable(recipe) { if (it == 1) 9 else 8 })
    }
}
