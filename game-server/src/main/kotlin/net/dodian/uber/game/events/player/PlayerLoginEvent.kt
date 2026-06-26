package net.dodian.uber.game.events.player

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class PlayerLoginEvent(
    val client: Client,
) : GameEvent
