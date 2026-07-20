package net.dodian.uber.game.skill.fishing

import net.dodian.uber.game.engine.systems.skills.asSkillPlayer
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.skills.fishing.FishingModule

/**
 * Legacy [Client]-facing entry points. All spot data, interaction bindings, and the
 * actual fishing cycle now live in [FishingModule] (`:skills:fishing`); this object
 * only adapts the few call sites that still hold a [Client] instead of a
 * [net.dodian.uber.game.api.plugin.skills.SkillPlayer].
 */
object Fishing {
    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun stopFromReset(client: Client, fullReset: Boolean) {
        FishingModule.stopAction(client.asSkillPlayer(), ActionStopReason.USER_INTERRUPT)
    }
}
