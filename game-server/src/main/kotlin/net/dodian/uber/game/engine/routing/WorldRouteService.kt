package net.dodian.uber.game.engine.routing

import java.util.concurrent.atomic.AtomicLong
import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.objects.WorldObject
import org.rsmod.routefinder.LineRouteFinding
import org.rsmod.routefinder.LineValidator
import org.rsmod.routefinder.RayCast
import org.rsmod.routefinder.Route
import org.rsmod.routefinder.RouteFinding
import org.rsmod.routefinder.StepValidator
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.collision.CollisionStrategy
import org.rsmod.routefinder.flag.CollisionFlag
import org.rsmod.routefinder.reach.ReachStrategy

/** Authoritative world collision and routing boundary backed entirely by RS Mod routefinder. */
object WorldRouteService {
    @Volatile private var flags = CollisionFlagMap()
    private val generation = AtomicLong()
    private val routeWorkspace = ThreadLocal.withInitial { Workspace(-1, flags, RouteFinding(flags)) }

    data class Metrics(
        val activeZones: Int,
        val materializedZones: Int,
        val activePages: Int,
        val payloadBytes: Long,
        val estimatedDirectoryBytes: Long,
        val estimatedRetainedBytes: Long,
    )

    fun clear() {
        flags = CollisionFlagMap()
        generation.incrementAndGet()
        routeWorkspace.remove()
    }

    fun collisionMap(): CollisionFlagMap = flags

    fun metrics(): Metrics = flags.metrics().let { metrics ->
        Metrics(
            activeZones = metrics.openZones,
            materializedZones = metrics.materializedZones,
            activePages = metrics.activePages,
            payloadBytes = metrics.payloadBytes,
            estimatedDirectoryBytes = metrics.estimatedDirectoryBytes,
            estimatedRetainedBytes = metrics.estimatedRetainedBytes,
        )
    }

    fun getFlags(x: Int, y: Int, level: Int): Int = flags[x, y, level]

    fun allocateZone(x: Int, y: Int, level: Int) {
        flags.markZoneAllocated(x, y, level)
    }

    fun allocateRegionPlane(x: Int, y: Int, level: Int) {
        flags.markRegionPlaneAllocated(x, y, level)
    }

    fun addFlag(x: Int, y: Int, level: Int, mask: Int) {
        flags.add(x, y, level, mask)
    }

    fun removeFlag(x: Int, y: Int, level: Int, mask: Int) {
        flags.remove(x, y, level, mask)
    }

    fun markTerrainBlocked(x: Int, y: Int, level: Int, add: Boolean = true) =
        update(x, y, level, CollisionFlag.BLOCK_WALK, add)

    fun markBridge(x: Int, y: Int, level: Int, add: Boolean = true) =
        update(x, y, level, CollisionFlag.ROOF, add)

    fun markOccupant(level: Int, x: Int, y: Int, width: Int, length: Int, impenetrable: Boolean, routeBlocker: Boolean = false, add: Boolean) {
        val mask = CollisionFlag.LOC or
            (if (impenetrable) CollisionFlag.LOC_PROJ_BLOCKER else 0) or
            (if (routeBlocker) CollisionFlag.LOC_ROUTE_BLOCKER else 0)
        for (tileX in x until x + width) for (tileY in y until y + length) update(tileX, tileY, level, mask, add)
    }

    fun markGroundDecoration(level: Int, x: Int, y: Int, add: Boolean) =
        update(x, y, level, CollisionFlag.GROUND_DECOR, add)

    fun markWall(direction: CollisionDirection, level: Int, x: Int, y: Int, type: Int, impenetrable: Boolean, add: Boolean) {
        when (type) {
            0 -> markStraightWall(direction, level, x, y, impenetrable, add)
            2 -> {
                markStraightWall(direction, level, x, y, impenetrable, add)
                markStraightWall(direction.clockwise(), level, x, y, impenetrable, add)
            }
            1, 3 -> markDiagonalWall(direction, level, x, y, impenetrable, add)
        }
    }

    fun canTravel(level: Int, x: Int, y: Int, dx: Int, dy: Int, size: Int = 1, extraFlag: Int = 0): Boolean =
        StepValidator(flags).canTravel(level, x, y, dx, dy, size.coerceAtLeast(1), extraFlag, CollisionStrategy.Normal)

    /** Compatibility shape: x/y are the destination and dx/dy identify the step. */
    fun traversable(x: Int, y: Int, level: Int, dx: Int, dy: Int, size: Int = 1): Boolean =
        dx == 0 && dy == 0 || canTravel(level, x - dx, y - dy, dx, dy, size)

    fun canMove(startX: Int, startY: Int, endX: Int, endY: Int, level: Int, size: Int): Boolean {
        var x = startX
        var y = startY
        while (x != endX || y != endY) {
            val dx = Integer.compare(endX, x)
            val dy = Integer.compare(endY, y)
            if (!canTravel(level, x, y, dx, dy, size)) return false
            x += dx
            y += dy
        }
        return true
    }

    fun isTileBlocked(x: Int, y: Int, level: Int): Boolean =
        getFlags(x, y, level) and (CollisionFlag.LOC or CollisionFlag.GROUND_DECOR or CollisionFlag.BLOCK_WALK) != 0

    fun findRoute(
        level: Int,
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
        srcSize: Int = 1,
        destWidth: Int = 1,
        destLength: Int = 1,
        locAngle: Int = 0,
        locShape: Int = -1,
        moveNear: Boolean = true,
        blockAccessFlags: Int = 0,
    ): Route = workspace().routeFinding.findRoute(
        level = level,
        srcX = srcX,
        srcZ = srcY,
        destX = destX,
        destZ = destY,
        srcSize = srcSize.coerceAtLeast(1),
        destWidth = destWidth.coerceAtLeast(1),
        destLength = destLength.coerceAtLeast(1),
        locAngle = locAngle and 3,
        locShape = locShape,
        moveNear = moveNear,
        blockAccessFlags = blockAccessFlags,
    )

    fun hasLineOfSight(source: Position, target: Position, sourceSize: Int = 1, targetSize: Int = 1): Boolean =
        source.z == target.z && LineValidator(flags).hasLineOfSight(
            source.z, source.x, source.y, target.x, target.y,
            sourceSize.coerceAtLeast(1), sourceSize.coerceAtLeast(1), targetSize.coerceAtLeast(1), targetSize.coerceAtLeast(1),
        )

    fun hasLineOfWalk(source: Position, target: Position, sourceSize: Int = 1, targetSize: Int = 1): Boolean =
        source.z == target.z && LineValidator(flags).hasLineOfWalk(
            source.z, source.x, source.y, target.x, target.y,
            sourceSize.coerceAtLeast(1), sourceSize.coerceAtLeast(1), targetSize.coerceAtLeast(1), targetSize.coerceAtLeast(1),
        )

    fun rayCast(source: Position, target: Position, lineOfSight: Boolean): RayCast =
        if (source.z != target.z) {
            RayCast.FAILED
        } else if (lineOfSight) {
            LineRouteFinding(flags).lineOfSight(source.z, source.x, source.y, target.x, target.y)
        } else {
            LineRouteFinding(flags).lineOfWalk(source.z, source.x, source.y, target.x, target.y)
        }

    fun reachedObject(source: Position, sourceSize: Int, worldObject: WorldObject): Boolean {
        if (source.z != worldObject.z) return false
        val definition = GameObjectData.forId(worldObject.id)
        return ReachStrategy.reached(
            flags = flags,
            level = source.z,
            srcX = source.x,
            srcZ = source.y,
            destX = worldObject.x,
            destZ = worldObject.y,
            destWidth = definition.sizeX.coerceAtLeast(1),
            destLength = definition.sizeY.coerceAtLeast(1),
            srcSize = sourceSize.coerceAtLeast(1),
            locAngle = worldObject.face and 3,
            locShape = worldObject.type,
            blockAccessFlags = definition.walkingFlag and 0xF,
        )
    }

    private fun workspace(): Workspace {
        val currentGeneration = generation.get()
        val currentFlags = flags
        val current = routeWorkspace.get()
        if (current.generation == currentGeneration && current.flags === currentFlags) return current
        return Workspace(currentGeneration, currentFlags, RouteFinding(currentFlags)).also(routeWorkspace::set)
    }

    private fun update(x: Int, y: Int, level: Int, mask: Int, add: Boolean) {
        if (add) addFlag(x, y, level, mask) else removeFlag(x, y, level, mask)
    }

    private fun markStraightWall(direction: CollisionDirection, level: Int, x: Int, y: Int, impenetrable: Boolean, add: Boolean) {
        val own = direction.wallFlag
        val other = direction.opposite().wallFlag
        update(x, y, level, own or if (impenetrable) own.shl(9) else 0, add)
        update(x + direction.dx, y + direction.dy, level, other or if (impenetrable) other.shl(9) else 0, add)
    }

    private fun markDiagonalWall(direction: CollisionDirection, level: Int, x: Int, y: Int, impenetrable: Boolean, add: Boolean) {
        val diagonal = direction.diagonalFlag
        val (dx, dy, opposite) =
            when (direction) {
                CollisionDirection.WEST -> Triple(-1, 1, CollisionFlag.WALL_SOUTH_EAST)
                CollisionDirection.NORTH -> Triple(1, 1, CollisionFlag.WALL_SOUTH_WEST)
                CollisionDirection.EAST -> Triple(1, -1, CollisionFlag.WALL_NORTH_WEST)
                CollisionDirection.SOUTH -> Triple(-1, -1, CollisionFlag.WALL_NORTH_EAST)
                else -> return
            }
        update(x, y, level, diagonal or if (impenetrable) diagonal.shl(9) else 0, add)
        update(x + dx, y + dy, level, opposite or if (impenetrable) opposite.shl(9) else 0, add)
    }

    private data class Workspace(val generation: Long, val flags: CollisionFlagMap, val routeFinding: RouteFinding)
}

enum class CollisionDirection(val dx: Int, val dy: Int, val wallFlag: Int, val diagonalFlag: Int) {
    NORTH_WEST(-1, 1, 0, CollisionFlag.WALL_NORTH_WEST),
    NORTH(0, 1, CollisionFlag.WALL_NORTH, CollisionFlag.WALL_NORTH_EAST),
    NORTH_EAST(1, 1, 0, CollisionFlag.WALL_NORTH_EAST),
    WEST(-1, 0, CollisionFlag.WALL_WEST, CollisionFlag.WALL_NORTH_WEST),
    EAST(1, 0, CollisionFlag.WALL_EAST, CollisionFlag.WALL_SOUTH_EAST),
    SOUTH_WEST(-1, -1, 0, CollisionFlag.WALL_SOUTH_WEST),
    SOUTH(0, -1, CollisionFlag.WALL_SOUTH, CollisionFlag.WALL_SOUTH_WEST),
    SOUTH_EAST(1, -1, 0, CollisionFlag.WALL_SOUTH_EAST),
    NONE(0, 0, 0, 0);

    fun opposite(): CollisionDirection = entries.first { it.dx == -dx && it.dy == -dy }
    fun clockwise(): CollisionDirection = when (this) { WEST -> NORTH; NORTH -> EAST; EAST -> SOUTH; SOUTH -> WEST; else -> this }

    companion object {
        val WNES = listOf(WEST, NORTH, EAST, SOUTH)
        fun fromDelta(dx: Int, dy: Int): CollisionDirection = entries.firstOrNull { it.dx == dx && it.dy == dy } ?: NONE
    }
}
