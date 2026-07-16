package net.dodian.uber.skills.prayer
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.skills.testkit.LiveSkillModuleFixture
import org.junit.jupiter.api.Test
class PrayerModuleRuntimeTest { @Test fun livePluginIsRegistered() = LiveSkillModuleFixture.requirePlugin(PrayerModule.descriptor.id, Skill.PRAYER) }
