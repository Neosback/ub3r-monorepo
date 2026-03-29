package net.dodian.game.content.skills.prayer

import net.dodian.game.content.skills.core.runtime.SkillActionState

data class PrayerOfferingState(
    val boneItemId: Int,
    val altarX: Int,
    val altarY: Int,
) : SkillActionState
