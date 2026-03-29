package net.dodian.game.event.events

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.event.GameEvent

data class CommandEvent(
    val client: Client,
    val rawCommand: String,
    val parsedArgs: List<String>,
) : GameEvent
