package net.dodian.game.content.skills.woodcutting

import net.dodian.cache.`object`.GameObjectData
import net.dodian.game.model.Position

data class WoodcuttingState(
    val treeObjectId: Int,
    val treePosition: Position,
    val objectData: GameObjectData?,
    val startedCycle: Long,
    val resourcesGathered: Int,
)
