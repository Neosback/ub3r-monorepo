package net.dodian.uber.skills.skillguide
import net.dodian.uber.game.model.player.skills.Skill
import net.dodian.uber.game.skill.skillguide.SkillGuideData
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
class SkillguideModuleRuntimeTest { @Test fun liveSkillGuideDataIsAvailable() { assertNotNull(SkillGuideData.find(Skill.MINING.id)) } }
