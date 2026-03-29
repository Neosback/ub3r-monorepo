package net.dodian.game.content.skills.fletching

import net.dodian.game.content.skills.core.runtime.SkillActionRequest

data class FletchingRequest(
    val logIndex: Int,
    val productId: Int,
    val experience: Int,
    val amount: Int,
) : SkillActionRequest
