package net.dodian.game.event.events

import net.dodian.cache.`object`.GameObjectData
import net.dodian.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.event.GameEvent

data class ObjectClickEvent(
    val client: Client,
    val option: Int,
    val objectId: Int,
    val position: Position,
    val obj: GameObjectData?,
) : GameEvent
