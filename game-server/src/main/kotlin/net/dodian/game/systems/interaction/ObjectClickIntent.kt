package net.dodian.game.systems.interaction

import net.dodian.cache.`object`.GameObjectData
import net.dodian.cache.`object`.GameObjectDef
import net.dodian.game.model.Position

data class ObjectClickIntent(
    override val opcode: Int,
    override val createdCycle: Long,
    val option: Int,
    val objectId: Int,
    val objectPosition: Position,
    val objectData: GameObjectData?,
    val objectDef: GameObjectDef?,
) : InteractionIntent
