package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.api.plugin.ContentMaturity
import net.dodian.uber.game.model.player.skills.Skill
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class SkillPluginRegistryTest {
    @Test
    fun `context route binding validates and resolves`() {
        val engine = SkillPluginRegistryEngine()
        val plugin = plugin("context-route") {
            skillPlugin("Context route", Skill.MINING) {
                objectClick(PolicyPreset.GATHERING, 1, 7485) { interaction: SkillObjectInteraction ->
                    interaction.option == 1 && interaction.objectId == 7485
                }
            }
        }

        assertDoesNotThrow { engine.validate(listOf(plugin)) }
    }

    @Test
    fun `duplicate typed routes fail during validation`() {
        val first = plugin("first") {
            skillPlugin("First", Skill.MINING) {
                objectClick(PolicyPreset.GATHERING, 1, 7485) { _: SkillObjectInteraction -> true }
            }
        }
        val duplicate = plugin("duplicate") {
            skillPlugin("Duplicate", Skill.WOODCUTTING) {
                objectClick(PolicyPreset.GATHERING, 1, 7485) { _: SkillObjectInteraction -> true }
            }
        }

        assertThrows<IllegalArgumentException> {
            SkillPluginRegistryEngine().validate(listOf(first, duplicate))
        }
    }

    @Test
    fun `invalid typed route input fails at declaration`() {
        assertThrows<IllegalArgumentException> {
            skillPlugin("Invalid", Skill.MINING) {
                objectClick(PolicyPreset.GATHERING, option = 6, 7485) { _: SkillObjectInteraction -> true }
            }
        }
    }

    @Test
    fun `manifest derives exactly the typed route inventory`() {
        val definition = skillPlugin("Manifested", Skill.MINING) {
            objectClick(PolicyPreset.GATHERING, 1, 7485, 7486) { _: SkillObjectInteraction -> true }
            itemOnItem(PolicyPreset.PRODUCTION, 100, 200) { _: SkillItemOnItemInteraction -> true }
        }

        val manifest = definition.manifest(
            id = "skill.manifested",
            owner = "test",
            maturity = ContentMaturity.STABLE,
        )

        org.junit.jupiter.api.Assertions.assertEquals(
            setOf("object:1:7485", "object:1:7486", "item-on-item:100:200"),
            manifest.declaredRouteKeys,
        )
    }

    private fun plugin(name: String, definition: () -> SkillPluginDefinition): SkillPlugin =
        object : SkillPlugin {
            override val definition: SkillPluginDefinition = definition()
            override val pluginMetadata = net.dodian.uber.game.api.plugin.PluginModuleMetadata(
                name = name,
                description = name,
                version = "1.0.0",
                owner = "test",
            )
        }
}
