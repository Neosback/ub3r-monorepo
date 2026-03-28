package net.dodian.uber.game.content.skills.prayer

import net.dodian.uber.game.content.platform.SkillDataRegistry

data class PrayerBoneDefinition(
    val itemId: Int,
    val experience: Int,
)

object PrayerDefinitions {
    val bones: List<PrayerBoneDefinition>
        get() = SkillDataRegistry.prayerBones()

    val altarObjectIds: IntArray
        get() = SkillDataRegistry.prayerAltarObjectIds()

    @JvmStatic
    fun findBone(itemId: Int): PrayerBoneDefinition? = bones.firstOrNull { it.itemId == itemId }
}
