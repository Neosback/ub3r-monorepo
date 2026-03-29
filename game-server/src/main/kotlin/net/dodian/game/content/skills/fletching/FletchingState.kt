package net.dodian.game.content.skills.fletching

import net.dodian.game.content.skills.core.runtime.SkillActionState

data class FletchingState(
    val logIndex: Int,
    val productId: Int = -1,
    val experience: Int = 0,
    val remaining: Int = 0,
) : SkillActionState {
    val isActive: Boolean
        get() = productId > 0 && remaining > 0
}
