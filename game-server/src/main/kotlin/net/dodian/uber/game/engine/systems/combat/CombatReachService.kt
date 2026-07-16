package net.dodian.uber.game.engine.systems.combat

import net.dodian.uber.game.engine.routing.WorldRouteService
import net.dodian.uber.game.model.entity.Entity

enum class CombatReachResult {
    READY,
    OUT_OF_RANGE,
    NO_LINE_OF_SIGHT,
    OVERLAPPING,
    WRONG_PLANE,
}

object CombatReachService {
    @JvmStatic
    fun evaluate(attacker: Entity, target: Entity, range: Int, projectile: Boolean): CombatReachResult {
        if (attacker.position.z != target.position.z) return CombatReachResult.WRONG_PLANE
        if (overlaps(attacker, target)) return CombatReachResult.OVERLAPPING
        if (gapDistance(attacker, target) > range) return CombatReachResult.OUT_OF_RANGE
        if (projectile && !WorldRouteService.hasLineOfSight(attacker.position, target.position, attacker.size, target.size)) {
            return CombatReachResult.NO_LINE_OF_SIGHT
        }
        return CombatReachResult.READY
    }

    @JvmStatic
    fun hasProjectileLineOfSight(attacker: Entity, target: Entity): Boolean =
        attacker.position.z == target.position.z && WorldRouteService.hasLineOfSight(attacker.position, target.position, attacker.size, target.size)

    private fun overlaps(first: Entity, second: Entity): Boolean {
        val firstSize = first.getSize().coerceAtLeast(1)
        val secondSize = second.getSize().coerceAtLeast(1)
        return first.position.x < second.position.x + secondSize &&
            first.position.x + firstSize > second.position.x &&
            first.position.y < second.position.y + secondSize &&
            first.position.y + firstSize > second.position.y
    }

    private fun gapDistance(first: Entity, second: Entity): Int {
        val firstSize = first.getSize().coerceAtLeast(1)
        val secondSize = second.getSize().coerceAtLeast(1)
        val dx = when {
            first.position.x + firstSize - 1 < second.position.x -> second.position.x - (first.position.x + firstSize - 1)
            second.position.x + secondSize - 1 < first.position.x -> first.position.x - (second.position.x + secondSize - 1)
            else -> 0
        }
        val dy = when {
            first.position.y + firstSize - 1 < second.position.y -> second.position.y - (first.position.y + firstSize - 1)
            second.position.y + secondSize - 1 < first.position.y -> first.position.y - (second.position.y + secondSize - 1)
            else -> 0
        }
        return maxOf(dx, dy)
    }
}
