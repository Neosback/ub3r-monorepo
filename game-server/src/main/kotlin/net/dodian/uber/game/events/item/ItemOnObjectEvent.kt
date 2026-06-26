package net.dodian.uber.game.events.item

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.events.GameEvent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client


data class ItemOnObjectEvent(
    val client: Client,
    val objectId: Int,
    val position: Position,
    val obj: GameObjectData?,
    val itemId: Int,
    val itemSlot: Int,
    val interfaceId: Int,
) : GameEvent
