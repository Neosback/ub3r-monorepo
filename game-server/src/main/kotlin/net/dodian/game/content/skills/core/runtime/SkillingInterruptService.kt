package net.dodian.game.content.skills.core.runtime

import net.dodian.game.event.GameEventBus
import net.dodian.game.event.events.skilling.SkillingActionStoppedEvent
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.systems.action.PlayerActionCancelReason
import net.dodian.game.content.skills.core.events.SkillActionInterruptEvent

object SkillingInterruptService {
    @JvmStatic
    fun stopReason(reason: PlayerActionCancelReason?): ActionStopReason = ActionStopReasonMapper.fromCancelReason(reason)

    @JvmStatic
    fun postStopped(player: Client, actionName: String, reason: PlayerActionCancelReason?) {
        GameEventBus.post(
            SkillingActionStoppedEvent(
                client = player,
                actionName = actionName,
                reason = stopReason(reason),
            ),
        )
        GameEventBus.post(SkillActionInterruptEvent(player, actionName, stopReason(reason)))
    }
}
