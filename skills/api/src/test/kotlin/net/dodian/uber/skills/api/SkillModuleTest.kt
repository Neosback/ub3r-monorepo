package net.dodian.uber.skills.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SkillModuleTest {
    @Test
    fun `recipe DSL consolidates a repeated material`() {
        val recipe = skillRecipe(200) {
            output(3)
            material(100, 2)
            material(100, 4)
        }

        assertEquals(3, recipe.outputAmount)
        assertEquals(listOf(SkillMaterial(100, 6)), recipe.materials)
    }

    @Test
    fun `module ids are stable and validated`() {
        assertEquals("skill.woodcutting", SkillModuleDescriptor("skill.woodcutting", "Woodcutting").id)
        assertThrows(IllegalArgumentException::class.java) {
            SkillModuleDescriptor("Woodcutting", "Woodcutting")
        }
    }
}
