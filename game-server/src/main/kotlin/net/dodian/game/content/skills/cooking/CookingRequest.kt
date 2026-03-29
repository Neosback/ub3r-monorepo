package net.dodian.game.content.skills.cooking

import net.dodian.game.content.skills.core.runtime.SkillActionRequest

data class CookingRequest(
    val itemId: Int,
    val cookIndex: Int,
    val amount: Int,
) : SkillActionRequest
