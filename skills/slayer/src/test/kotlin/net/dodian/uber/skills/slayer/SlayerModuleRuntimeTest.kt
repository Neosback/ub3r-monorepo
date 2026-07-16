package net.dodian.uber.skills.slayer
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class SlayerModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(SlayerModule.descriptor.id, Skill.SLAYER) }
