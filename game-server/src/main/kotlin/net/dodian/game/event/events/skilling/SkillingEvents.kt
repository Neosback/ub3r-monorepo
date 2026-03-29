package net.dodian.game.event.events.skilling

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.event.GameEvent
import net.dodian.game.content.skills.core.runtime.ActionStopReason

data class SkillingActionStartedEvent(
    val client: Client,
    val actionName: String,
) : GameEvent

data class SkillingActionCycleEvent(
    val client: Client,
    val actionName: String,
) : GameEvent

data class SkillingActionSucceededEvent(
    val client: Client,
    val actionName: String,
) : GameEvent

data class SkillingActionStoppedEvent(
    val client: Client,
    val actionName: String,
    val reason: ActionStopReason,
) : GameEvent
