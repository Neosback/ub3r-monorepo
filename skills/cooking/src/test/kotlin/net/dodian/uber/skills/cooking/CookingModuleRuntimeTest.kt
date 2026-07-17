package net.dodian.uber.skills.cooking

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

class CookingModuleRuntimeTest {
    @Test
    fun `seaweed range conversion dispatches through live transactional route`() {
        LiveSkillModuleFixture.requirePlugin(CookingModule.descriptor.id, Skill.COOKING)
        val plugin = skillPlugin("Cooking fixture", Skill.COOKING) {
            itemOnObject(PolicyPreset.PRODUCTION, 26181, itemIds = intArrayOf(401)) { interaction ->
                val amount = interaction.player.inventory.amount(401)
                val committed = amount > 0 && interaction.player.inventory.transaction { remove(401, amount); add(1781, amount) }
                if (committed) interaction.player.ui.message("You burn all your seaweed into ashes.")
                true
            }
        }
        val binding = plugin.itemOnObjectBindings.single { 26181 in it.objectIds }
        val player = FakeSkillPlayer(mapOf(401 to 4))

        val handled = binding.handler(SkillItemOnObjectInteraction(player, SkillObjectRef(26181, SkillPosition(0, 0)), 401, 0, 3214))

        assertTrue(handled)
        assertEquals(0, player.amount(401))
        assertEquals(4, player.amount(1781))
        assertEquals(0, player.skills.experience(Skill.COOKING))
        assertEquals(listOf("You burn all your seaweed into ashes."), player.messages)
        assertEquals(null, player.activeActionName())
    }
}
