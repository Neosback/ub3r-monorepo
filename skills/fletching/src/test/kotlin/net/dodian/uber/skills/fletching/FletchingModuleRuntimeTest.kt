package net.dodian.uber.skills.fletching
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class FletchingModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(FletchingModule.descriptor.id, Skill.FLETCHING) }
