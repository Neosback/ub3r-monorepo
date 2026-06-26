package net.dodian.uber.game.events.item

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class ItemOnItemEvent(
    val client: Client,
    val itemUsedSlot: Int,
    val itemUsedWithSlot: Int,
    val itemUsedId: Int,
    val itemUsedWithId: Int,
) : GameEvent
