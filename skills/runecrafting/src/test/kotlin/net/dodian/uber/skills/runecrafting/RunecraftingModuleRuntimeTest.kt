package net.dodian.uber.skills.runecrafting
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class RunecraftingModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(RunecraftingModule.descriptor.id, Skill.RUNECRAFTING) }
