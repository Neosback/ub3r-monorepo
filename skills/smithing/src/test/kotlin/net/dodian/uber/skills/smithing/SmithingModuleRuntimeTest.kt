package net.dodian.uber.skills.smithing

import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.plugin.skills.SkillItemOnObjectInteraction
import net.dodian.uber.game.api.plugin.skills.SkillObjectRef
import net.dodian.uber.game.api.plugin.skills.SkillPosition
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.FakeSkillPlayer
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SmithingModuleRuntimeTest {
    @Test
    fun `dragonfire shield assembly dispatches through live transactional route`() {
        LiveSkillModuleFixture.requirePlugin(SmithingModule.descriptor.id, Skill.SMITHING)
        val plugin = skillPlugin("Smithing fixture", Skill.SMITHING) {
            itemOnObject(PolicyPreset.PRODUCTION, 2097, itemIds = intArrayOf(1540)) { interaction ->
                val committed = interaction.player.inventory.transaction { remove(1540); remove(11286); add(11284) }
                if (committed) { interaction.player.skills.gainXp(15000, Skill.SMITHING); interaction.player.ui.message("Your smithing craft made a Dragonfire shield out of the visage.") }
                true
            }
        }
        val binding = plugin.itemOnObjectBindings.first { 2097 in it.objectIds }
        val player = FakeSkillPlayer(mapOf(2347 to 1, 1540 to 1, 11286 to 1)).apply { setLevel(Skill.SMITHING, 99) }

        val handled = binding.handler(SkillItemOnObjectInteraction(player, SkillObjectRef(2097, SkillPosition(0, 0)), 1540, 1, 3214))

        assertTrue(handled)
        assertEquals(0, player.amount(1540))
        assertEquals(0, player.amount(11286))
        assertEquals(1, player.amount(11284))
        assertEquals(15000, player.skills.experience(Skill.SMITHING))
        assertEquals(listOf("Your smithing craft made a Dragonfire shield out of the visage."), player.messages)
        assertEquals(null, player.activeActionName())
    }
}
