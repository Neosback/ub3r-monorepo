package net.dodian.uber.game.events

import net.dodian.uber.game.model.entity.player.Client


data class PlayerDeathEvent(
    val player: Client,
    val cycle: Long,
) : GameEvent