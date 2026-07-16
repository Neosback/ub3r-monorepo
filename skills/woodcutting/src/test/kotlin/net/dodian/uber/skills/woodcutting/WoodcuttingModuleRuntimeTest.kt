package net.dodian.uber.skills.woodcutting
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class WoodcuttingModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(WoodcuttingModule.descriptor.id, Skill.WOODCUTTING) }
