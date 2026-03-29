package net.dodian.game.content.skills.mining

import net.dodian.game.model.Position

data class MiningState(
    val rockObjectId: Int,
    val rockPosition: Position,
    val startedCycle: Long,
    val resourcesGathered: Int,
)
