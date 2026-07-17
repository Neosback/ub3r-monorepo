package net.dodian.uber.skills.fletching

import net.dodian.uber.game.api.plugin.skills.skillPlugin
import net.dodian.uber.game.api.plugin.skills.startProduction
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.plugin.skills.SkillItemOnItemInteraction
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.api.SkillMultiSelection
import net.dodian.uber.skills.testkit.FakeSkillPlayer
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FletchingModuleRuntimeTest {
    @Test
    fun `knife on logs opens and executes the live transactional route`() {
        LiveSkillModuleFixture.requirePlugin(FletchingModule.descriptor.id, Skill.FLETCHING)
        val plugin = skillPlugin("Fletching fixture", Skill.FLETCHING) {
            itemOnItem(PolicyPreset.PRODUCTION, 946, 1521) { interaction ->
                val recipe = net.dodian.uber.skills.api.skillRecipe("fletching.logs", 54) { material(1521); experience(102); delay(1); success("You carefully cut the logs.") }
                interaction.player.production.open(net.dodian.uber.skills.api.SkillMultiConfig("fletching.logs", entries = listOf(net.dodian.uber.skills.api.SkillMultiEntry(recipe)))) { selection -> interaction.player.startProduction(recipe, selection.amount, Skill.FLETCHING) }
            }
        }
        val binding = plugin.itemOnItemBindings.single { it.leftItemId == 946 && it.rightItemId == 1521 }
        val player = FakeSkillPlayer(mapOf(946 to 1, 1521 to 2)).apply { setLevel(Skill.FLETCHING, 99) }

        assertTrue(binding.handler(SkillItemOnItemInteraction(player, 946, 1521)))
        val menu = player.production.pending()!!
        val shortbow = menu.entries.first()
        assertTrue(player.production.select(SkillMultiSelection(menu.key, shortbow.recipe.key, 2)))
        player.advanceTicks(4)

        assertEquals(0, player.amount(1521))
        assertEquals(2, player.amount(54))
        assertEquals(204, player.skills.experience(Skill.FLETCHING))
        assertEquals(2, player.messages.size)
        assertEquals(null, player.activeActionName())
    }
}
