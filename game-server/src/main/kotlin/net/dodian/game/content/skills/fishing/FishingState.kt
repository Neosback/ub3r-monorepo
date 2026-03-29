package net.dodian.game.content.skills.fishing

import net.dodian.game.content.skills.core.runtime.SkillActionState

data class FishingState(
    val spotIndex: Int,
    val gatheredCount: Int = 0,
) : SkillActionState
