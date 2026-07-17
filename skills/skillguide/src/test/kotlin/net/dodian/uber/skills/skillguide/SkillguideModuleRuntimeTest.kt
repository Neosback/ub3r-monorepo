package net.dodian.uber.skills.skillguide
import net.dodian.uber.skills.api.SkillModuleDescriptor
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
class SkillguideModuleRuntimeTest { @Test fun moduleDescriptorIsValid() { assertNotNull(SkillModuleDescriptor("skill.skillguide", "Skill guide")) } }
