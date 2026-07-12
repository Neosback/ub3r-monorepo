package net.dodian.uber.game.engine.systems.pathing.collision

import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.Entity
import kotlin.math.abs

object ProjectileLineService {
    data class Footprint(val x: Int, val y: Int, val size: Int, val z: Int) {
        init {
            require(size > 0) { "Footprint size must be positive" }
        }

        fun tiles(): Sequence<Position> = sequence {
            for (tileX in x until x + size) {
                for (tileY in y until y + size) {
                    yield(Position(tileX, tileY, z))
                }
            }
        }
    }

    data class TraceResult(
        val clear: Boolean,
        val source: Position,
        val target: Position,
        val blockedAt: Position? = null,
        val stepX: Int = 0,
        val stepY: Int = 0,
        val previousFlags: Int = 0,
        val flags: Int = 0,
    )

    @JvmStatic
    @JvmOverloads
    fun hasLineOfSight(source: Entity, target: Entity, collision: CollisionManager = CollisionManager.global()): Boolean =
        trace(
            Footprint(source.position.x, source.position.y, source.getSize().coerceAtLeast(1), source.position.z),
            Footprint(target.position.x, target.position.y, target.getSize().coerceAtLeast(1), target.position.z),
            collision,
        ).clear

    @JvmStatic
    @JvmOverloads
    fun hasLineOfSight(source: Footprint, target: Footprint, collision: CollisionManager = CollisionManager.global()): Boolean =
        trace(source, target, collision).clear

    @JvmStatic
    @JvmOverloads
    fun trace(source: Footprint, target: Footprint, collision: CollisionManager = CollisionManager.global()): TraceResult {
        val sourceTile = Position(source.x, source.y, source.z)
        val targetTile = Position(target.x, target.y, target.z)
        if (source.z != target.z) {
            return TraceResult(false, sourceTile, targetTile)
        }

        val sourceTiles = source.tiles().toList()
        val targetTiles = target.tiles().toList()
        val candidates = sourceTiles.flatMap { start -> targetTiles.map { end -> start to end } }
            .sortedWith(compareBy<Pair<Position, Position>>(
                { maxOf(abs(it.second.x - it.first.x), abs(it.second.y - it.first.y)) },
                { abs(it.second.x - it.first.x) + abs(it.second.y - it.first.y) },
                { it.first.x }, { it.first.y }, { it.second.x }, { it.second.y },
            ))

        var firstBlocked: TraceResult? = null
        for ((start, end) in candidates) {
            val result = traceTiles(start, end, collision)
            if (result.clear) return result
            if (firstBlocked == null) firstBlocked = result
        }
        return firstBlocked ?: TraceResult(false, sourceTile, targetTile)
    }

    @JvmStatic
    @JvmOverloads
    fun hasLineOfSight(start: Position, end: Position, collision: CollisionManager = CollisionManager.global()): Boolean {
        return traceTiles(start, end, collision).clear
    }

    private fun traceTiles(start: Position, end: Position, collision: CollisionManager): TraceResult {
        if (start.z != end.z) return TraceResult(false, start, end)
        var x = start.x
        var y = start.y
        val nx = abs(end.x - start.x)
        val ny = abs(end.y - start.y)
        val sx = Integer.signum(end.x - start.x)
        val sy = Integer.signum(end.y - start.y)
        var ix = 0
        var iy = 0

        while (ix < nx || iy < ny) {
            val lhs = (1L + 2L * ix) * ny
            val rhs = (1L + 2L * iy) * nx
            val stepX: Int
            val stepY: Int
            when {
                lhs == rhs -> {
                    stepX = sx
                    stepY = sy
                    ix++
                    iy++
                }
                lhs < rhs -> {
                    stepX = sx
                    stepY = 0
                    ix++
                }
                else -> {
                    stepX = 0
                    stepY = sy
                    iy++
                }
            }
            x += stepX
            y += stepY
            if (!collision.projectileTraversable(x, y, start.z, stepX, stepY)) {
                return TraceResult(
                    clear = false,
                    source = start,
                    target = end,
                    blockedAt = Position(x, y, start.z),
                    stepX = stepX,
                    stepY = stepY,
                    previousFlags = collision.getFlags(x - stepX, y - stepY, start.z),
                    flags = collision.getFlags(x, y, start.z),
                )
            }
        }
        return TraceResult(true, start, end)
    }
}
