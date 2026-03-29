package net.dodian.game.content.skills.fishing

import net.dodian.game.content.skills.core.runtime.SkillActionRequest

data class FishingRequest(
    val spotIndex: Int,
) : SkillActionRequest
