package net.dodian.uber.game.events.widget

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class CommandEvent(
    val client: Client,
    val rawCommand: String,
    val parsedArgs: List<String>,
) : GameEvent
