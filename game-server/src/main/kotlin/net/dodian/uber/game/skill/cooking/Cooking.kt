package net.dodian.uber.game.skill.cooking

import net.dodian.uber.game.engine.systems.skills.asSkillPlayer
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.skill.runtime.action.ActionStopReason
import net.dodian.uber.skills.cooking.CookingModule

/**
 * Legacy [Client]-facing entry points. All recipe data, interaction bindings, and the
 * actual cook cycle now live in [CookingModule] (`:skills:cooking`); this object only
 * adapts the few call sites that still hold a [Client] instead of a [net.dodian.uber.game.api.plugin.skills.SkillPlayer].
 */
object Cooking {
    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun stopFromReset(client: Client, fullReset: Boolean) {
        CookingModule.stopAction(client.asSkillPlayer(), ActionStopReason.USER_INTERRUPT)
    }
}
