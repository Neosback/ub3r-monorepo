package net.dodian.game.content.skills.cooking

import net.dodian.game.content.skills.core.runtime.SkillActionState

data class CookingState(
    val itemId: Int,
    val cookIndex: Int,
    val remaining: Int,
) : SkillActionState
