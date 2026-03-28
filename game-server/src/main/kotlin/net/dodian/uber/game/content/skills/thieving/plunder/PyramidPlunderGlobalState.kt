package net.dodian.uber.game.content.skills.thieving.plunder

import net.dodian.uber.game.content.platform.SkillDataRegistry
import net.dodian.uber.game.model.Position

data class PyramidPlunderGlobalState(
    val allDoors: Array<Position> =
        SkillDataRegistry
            .thievingPlunderData()
            .allDoors
            .map { Position(it.x, it.y, it.z) }
            .toTypedArray(),
    val nextRoom: IntArray = IntArray(7),
    val start: Position =
        SkillDataRegistry
            .thievingPlunderData()
            .start
            .let { Position(it.x, it.y, it.z) },
    val end: Position =
        SkillDataRegistry
            .thievingPlunderData()
            .end
            .let { Position(it.x, it.y, it.z) },
    var currentDoor: Position? = null,
) {
    val roomEntrances: Array<Position> =
        SkillDataRegistry
            .thievingPlunderData()
            .roomEntrances
            .map { Position(it.x, it.y, it.z) }
            .toTypedArray()
}
