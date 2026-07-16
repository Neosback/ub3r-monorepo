package net.dodian.uber.skills.mining
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class MiningModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(MiningModule.descriptor.id, Skill.MINING) }
