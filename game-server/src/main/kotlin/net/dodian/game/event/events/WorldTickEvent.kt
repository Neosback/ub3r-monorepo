package net.dodian.game.event.events

import net.dodian.game.event.GameEvent

data class WorldTickEvent(
    val cycle: Int,
) : GameEvent
