package net.dodian.uber.game.engine.systems.pathing.collision

import net.dodian.uber.game.model.Position
import kotlin.math.abs

object ProjectileLineService {
    @JvmStatic
    @JvmOverloads
    fun hasLineOfSight(start: Position, end: Position, collision: CollisionManager = CollisionManager.global()): Boolean {
        if (start.z != end.z) return false
        var x = start.x
        var y = start.y
        val dx = abs(end.x - start.x)
        val dy = abs(end.y - start.y)
        val sx = Integer.signum(end.x - start.x)
        val sy = Integer.signum(end.y - start.y)
        var error = dx - dy
        while (x != end.x || y != end.y) {
            val doubled = error * 2
            var stepX = 0
            var stepY = 0
            if (doubled > -dy) { error -= dy; stepX = sx }
            if (doubled < dx) { error += dx; stepY = sy }
            x += stepX
            y += stepY
            if (!collision.projectileTraversable(x, y, start.z, stepX, stepY)) return false
        }
        return true
    }
}