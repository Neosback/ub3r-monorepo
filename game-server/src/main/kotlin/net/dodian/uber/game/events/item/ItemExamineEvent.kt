package net.dodian.uber.game.events.item

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


@Suppress("unused")
data class ItemExamineEvent(
    val client: Client,
    val itemId: Int,
    val contextValue: Int,
) : GameEvent