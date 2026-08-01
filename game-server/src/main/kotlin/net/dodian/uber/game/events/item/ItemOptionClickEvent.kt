package net.dodian.uber.game.events.item

import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.entity.player.Client


data class ItemOptionClickEvent(
    val client: Client,
    val option: Int,
    val itemId: Int,
    val itemSlot: Int,
    val interfaceId: Int,
) : GameEvent