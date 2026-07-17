package net.dodian.uber.game.api.plugin.skills

import net.dodian.uber.skills.api.SkillMultiConfig
import net.dodian.uber.skills.api.SkillMultiSelection

/** Engine-owned pending selection; content supplies only the typed completion callback. */
class PendingSkillMulti(
    val config: SkillMultiConfig,
    val onSelected: (SkillMultiSelection) -> Unit,
)
