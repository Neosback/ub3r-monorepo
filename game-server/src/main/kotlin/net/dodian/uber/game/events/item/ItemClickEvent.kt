package net.dodian.uber.game.events.item

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class ItemClickEvent(
    val client: Client,
    val itemId: Int,
    val itemSlot: Int,
    val interfaceId: Int,
) : GameEvent
