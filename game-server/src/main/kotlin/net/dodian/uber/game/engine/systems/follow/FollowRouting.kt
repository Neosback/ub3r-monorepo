package net.dodian.uber.game.engine.systems.follow

import java.util.ArrayDeque
import kotlin.math.abs
import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.entity.player.Player
import net.dodian.uber.game.model.objects.WorldObject
import net.dodian.uber.game.engine.systems.pathing.AStarPathfindingAlgorithm
import net.dodian.uber.game.engine.systems.pathing.Heuristic
import net.dodian.uber.game.engine.systems.pathing.Node
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import net.dodian.uber.game.engine.systems.pathing.collision.InteractionReachService
import org.slf4j.LoggerFactory
import net.dodian.uber.game.Server
import net.dodian.uber.game.model.entity.npc.Npc

object FollowRouting {
    private val logger = LoggerFactory.getLogger(FollowRouting::class.java)
    private val collisionManager: CollisionManager
        get() = CollisionManager.global()
    private val pathfinding =
        AStarPathfindingAlgorithm(
            collision = { x, y, z, dx, dy -> collisionManager.traversable(x, y, z, dx, dy) },
            heuristic = Heuristic.EUCLIDEAN,
        )

    @JvmStatic
    fun enqueueRandomCardinalStep(follower: Client, z: Int): Boolean {
        val fx = follower.position.x
        val fy = follower.position.y
        val step = CARDINAL_OFFSETS.shuffled().firstOrNull { (dx, dy) -> isTraversableStep(fx, fy, dx, dy, z) } ?: return false
        val baseX = follower.mapRegionX * 8
        val baseY = follower.mapRegionY * 8
        follower.newWalkCmdSteps = 1
        follower.newWalkCmdIsRunning = false
        follower.newWalkCmdX[0] = fx + step.first - baseX
        follower.newWalkCmdY[0] = fy + step.second - baseY
        follower.tmpNWCX[0] = follower.newWalkCmdX[0]
        follower.tmpNWCY[0] = follower.newWalkCmdY[0]
        return true
    }

    @JvmStatic
    fun isTileOccupied(x: Int, y: Int, z: Int, ignoreNpc: Npc? = null): Boolean {
        for (p in net.dodian.uber.game.engine.systems.world.player.PlayerRegistry.players) {
            if (p != null && p.isActive && !p.disconnected && p.position.z == z && p.position.x == x && p.position.y == y) {
                return true
            }
        }
        for (n in Server.npcManager.getNpcs()) {
            if (n != null && n.isAlive && n != ignoreNpc && n.position.z == z) {
                val nx = n.position.x
                val ny = n.position.y
                val size = n.getSize()
                if (x >= nx && x < nx + size && y >= ny && y < ny + size) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    @JvmOverloads
    fun routeToEntityBoundary(
        follower: Client,
        targetX: Int,
        targetY: Int,
        targetSize: Int,
        z: Int,
        preferredDestination: Pair<Int, Int>? = null,
        running: Boolean = follower.buttonOnRun,
        targetNpc: Npc? = null,
    ): Boolean {
        val normalizedTargetSize = targetSize.coerceAtLeast(1)
        val candidates = buildBoundaryCandidates(follower, targetX, targetY, normalizedTargetSize, preferredDestination)
        for (destination in candidates) {
            if (!isValidBoundaryDestination(destination, targetX, targetY, normalizedTargetSize, z)) {
                continue
            }
            if (isTileOccupied(destination.first, destination.second, z, targetNpc)) {
                continue
            }
            val searchStart = System.nanoTime()
            val path = pathfinding.find(follower.position.x, follower.position.y, destination.first, destination.second, z)
            FollowPathfindingTelemetry.recordSearch(
                durationNanos = System.nanoTime() - searchStart,
                foundPath = path.isNotEmpty(),
            )
            if (path.isEmpty()) {
                continue
            }
            val validated = validatePath(follower.position.x, follower.position.y, z, path)
            if (validated.isEmpty()) {
                continue
            }
            applyRoute(follower, validated, running)
            return true
        }
        return false
    }

    /**
     * Routes [follower] as Tarnish does for object waypoints: search against the object as an
     * interactable, then park on the closest reachable fallback tile when no strict interaction face
     * can be reached. The result distinguishes those cases so content dispatch can stay reach-gated.
     */
    @JvmStatic
    fun routeToObjectApproach(
        follower: Client,
        objectId: Int,
        objX: Int,
        objY: Int,
        z: Int,
        type: Int,
        rotation: Int,
        running: Boolean = follower.buttonOnRun,
    ): ObjectRouteResult {
        val definition = GameObjectData.forId(objectId)
        val rot = rotation and 0x3
        val sizeX = definition.getSizeX(rot).coerceAtLeast(1)
        val sizeY = definition.getSizeY(rot).coerceAtLeast(1)
        val worldObject = WorldObject(objectId, objX, objY, z, type, rotation)

        val alreadyReached = InteractionReachService.reachedObject(Position(follower.position.x, follower.position.y, z), worldObject)
        logger.info(
            "[ObjRoute] objectId={} obj=({},{},{}) size={}x{} type={} rot={} | player=({},{}) alreadyReached={}",
            objectId, objX, objY, z, sizeX, sizeY, type, rotation,
            follower.position.x, follower.position.y, alreadyReached,
        )
        if (alreadyReached) {
            return ObjectRouteResult(ObjectRouteStatus.ALREADY_REACHED, follower.position.x, follower.position.y)
        }

        val searchStart = System.nanoTime()
        val route = findObjectRoute(follower, worldObject, sizeX, sizeY)
        FollowPathfindingTelemetry.recordSearch(
            durationNanos = System.nanoTime() - searchStart,
            foundPath = route.status != ObjectRouteStatus.UNREACHABLE,
        )
        logger.info(
            "[ObjRoute] BFS result={} targetTile=({},{}) pathSize={}",
            route.status, route.targetX, route.targetY, route.path.size,
        )
        if (route.status == ObjectRouteStatus.UNREACHABLE) {
            return route
        }
        val validated = validatePath(follower.position.x, follower.position.y, z, route.path)
        // Only reject the path if NO steps at all could be validated (first step is blocked).
        // Tarnish trusts the pathfinder output and does not re-validate step-by-step — applying
        // a partial path is better than reporting UNREACHABLE when the player is almost there.
        logger.info(
            "[ObjRoute] validated={}/{} firstStep={} lastStep={}",
            validated.size, route.path.size,
            validated.firstOrNull()?.let { "(${it.x},${it.y})" } ?: "none",
            validated.lastOrNull()?.let { "(${it.x},${it.y})" } ?: "none",
        )
        if (validated.isEmpty()) {
            logger.info("[ObjRoute] validated path empty — first step blocked; walk queue reset")
            follower.resetWalkingQueue()
            return route.copy(path = validated)
        }
        applyRoute(follower, validated, running)
        return route.copy(path = validated)
    }

    @JvmStatic
    fun routeToObjectInteraction(
        follower: Client,
        objectId: Int,
        objX: Int,
        objY: Int,
        z: Int,
        type: Int,
        rotation: Int,
        running: Boolean = follower.buttonOnRun,
    ): Boolean {
        val result = routeToObjectApproach(follower, objectId, objX, objY, z, type, rotation, running)
        return result.status == ObjectRouteStatus.ALREADY_REACHED || result.status == ObjectRouteStatus.STRICT_REACHED
    }

    /** Legacy boolean wrapper around [routeToObjectApproach] for older callers. */
    @JvmStatic
    fun routeToObjectVicinity(
        follower: Client,
        objectId: Int,
        objX: Int,
        objY: Int,
        z: Int,
        rotation: Int,
        running: Boolean = follower.buttonOnRun,
    ): Boolean {
        val result = routeToObjectApproach(follower, objectId, objX, objY, z, DEFAULT_OBJECT_TYPE, rotation, running)
        return result.status != ObjectRouteStatus.UNREACHABLE
    }

    @JvmStatic
    fun routeToExactTile(
        follower: Client,
        destinationX: Int,
        destinationY: Int,
        z: Int,
        running: Boolean = follower.buttonOnRun,
    ): Boolean {
        if (collisionManager.isTileBlocked(destinationX, destinationY, z)) {
            return false
        }
        val searchStart = System.nanoTime()
        val path = pathfinding.find(follower.position.x, follower.position.y, destinationX, destinationY, z)
        FollowPathfindingTelemetry.recordSearch(
            durationNanos = System.nanoTime() - searchStart,
            foundPath = path.isNotEmpty(),
        )
        if (path.isEmpty()) {
            return false
        }
        val validated = validatePath(follower.position.x, follower.position.y, z, path)
        if (validated.isEmpty()) {
            return false
        }
        applyRoute(follower, validated, running)
        return true
    }

    private fun buildBoundaryCandidates(
        follower: Client,
        targetX: Int,
        targetY: Int,
        targetSize: Int,
        preferredDestination: Pair<Int, Int>?,
    ): List<Pair<Int, Int>> {
        val minX = targetX
        val minY = targetY
        val maxX = targetX + targetSize - 1
        val maxY = targetY + targetSize - 1
        val ranked = ArrayList<Pair<Int, Int>>()
        if (preferredDestination != null && isBoundaryTile(preferredDestination, minX, minY, maxX, maxY)) {
            ranked += preferredDestination
        }
        for (x in (minX - 1)..(maxX + 1)) {
            for (y in (minY - 1)..(maxY + 1)) {
                val tile = x to y
                if (isBoundaryTile(tile, minX, minY, maxX, maxY)) {
                    ranked += tile
                }
            }
        }

        val unique = LinkedHashSet<Pair<Int, Int>>(ranked.size)
        unique.addAll(ranked)
        return unique.sortedWith(
            compareBy<Pair<Int, Int>>(
                { if (preferredDestination != null && it == preferredDestination) 0 else 1 },
                { distanceToFootprint(it.first, it.second, minX, minY, maxX, maxY) },
                { abs(it.first - follower.position.x) + abs(it.second - follower.position.y) },
            ),
        )
    }

    private fun isBoundaryTile(
        tile: Pair<Int, Int>,
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
    ): Boolean {
        val (x, y) = tile
        if (x in minX..maxX && y in minY..maxY) {
            return false
        }
        return x in (minX - 1)..(maxX + 1) && y in (minY - 1)..(maxY + 1)
    }

    private fun isValidBoundaryDestination(
        destination: Pair<Int, Int>,
        targetX: Int,
        targetY: Int,
        targetSize: Int,
        z: Int,
    ): Boolean {
        val (x, y) = destination
        if (collisionManager.isTileBlocked(x, y, z)) {
            return false
        }

        val minX = targetX
        val minY = targetY
        val maxX = targetX + targetSize - 1
        val maxY = targetY + targetSize - 1
        val nearestX = clamp(x, minX, maxX)
        val nearestY = clamp(y, minY, maxY)
        val stepX = nearestX - x
        val stepY = nearestY - y
        if (abs(stepX) > 1 || abs(stepY) > 1) {
            return false
        }
        return collisionManager.traversable(nearestX, nearestY, z, stepX, stepY)
    }

    private fun validatePath(startX: Int, startY: Int, z: Int, path: ArrayDeque<Node>): ArrayDeque<Node> {
        val validated = ArrayDeque<Node>(path.size)
        var currentX = startX
        var currentY = startY
        for (step in path) {
            val dx = (step.x - currentX).coerceIn(-1, 1)
            val dy = (step.y - currentY).coerceIn(-1, 1)
            if (dx == 0 && dy == 0) {
                continue
            }
            if (!collisionManager.traversable(step.x, step.y, z, dx, dy)) {
                break
            }
            validated.add(step)
            currentX = step.x
            currentY = step.y
        }
        return validated
    }

    private fun applyRoute(
        follower: Client,
        path: ArrayDeque<Node>,
        running: Boolean,
    ) {
        val baseX = follower.mapRegionX * 8
        val baseY = follower.mapRegionY * 8
        val steps = minOf(path.size, Player.WALKING_QUEUE_SIZE)
        follower.newWalkCmdSteps = steps
        follower.newWalkCmdIsRunning = running

        var index = 0
        for (step in path) {
            if (index >= steps) {
                break
            }
            follower.newWalkCmdX[index] = step.x - baseX
            follower.newWalkCmdY[index] = step.y - baseY
            follower.tmpNWCX[index] = follower.newWalkCmdX[index]
            follower.tmpNWCY[index] = follower.newWalkCmdY[index]
            index++
        }
    }

    private fun isTraversableStep(fx: Int, fy: Int, dx: Int, dy: Int, z: Int): Boolean {
        if (dx == 0 && dy == 0) {
            return true
        }
        return collisionManager.traversable(fx + dx, fy + dy, z, dx, dy)
    }

    private fun distanceToFootprint(x: Int, y: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): Int {
        val dx = when {
            x < minX -> minX - x
            x > maxX -> x - maxX
            else -> 0
        }
        val dy = when {
            y < minY -> minY - y
            y > maxY -> y - maxY
            else -> 0
        }
        return dx + dy
    }

    private fun clamp(value: Int, min: Int, max: Int): Int = maxOf(min, minOf(max, value))

    private fun findObjectRoute(
        follower: Client,
        worldObject: WorldObject,
        sizeX: Int,
        sizeY: Int,
    ): ObjectRouteResult {
        val z = worldObject.z
        val baseX = follower.mapRegionX * 8
        val baseY = follower.mapRegionY * 8
        val srcLocalX = follower.position.x - baseX
        val srcLocalY = follower.position.y - baseY
        if (srcLocalX !in 0 until LOCAL_REGION_SIZE || srcLocalY !in 0 until LOCAL_REGION_SIZE) {
            return ObjectRouteResult(ObjectRouteStatus.UNREACHABLE)
        }

        val target = findBestInside(follower.position.x, follower.position.y, worldObject.x, worldObject.y, sizeX, sizeY)
        val targetLocalX = target.first - baseX
        val targetLocalY = target.second - baseY
        val via = IntArray(LOCAL_REGION_SIZE * LOCAL_REGION_SIZE)
        val cost = IntArray(LOCAL_REGION_SIZE * LOCAL_REGION_SIZE)
        val queueX = IntArray(MAX_OBJECT_ROUTE_TILES)
        val queueY = IntArray(MAX_OBJECT_ROUTE_TILES)
        var head = 0
        var tail = 0
        var foundIndex = -1
        var foundStatus = ObjectRouteStatus.UNREACHABLE

        val sourceIndex = localIndex(srcLocalX, srcLocalY)
        via[sourceIndex] = SOURCE_VIA
        cost[sourceIndex] = 1
        queueX[tail] = srcLocalX
        queueY[tail] = srcLocalY
        tail++

        while (head != tail && tail < MAX_OBJECT_ROUTE_TILES) {
            val curX = queueX[head]
            val curY = queueY[head]
            head++
            val curAbsX = baseX + curX
            val curAbsY = baseY + curY
            val currentIndex = localIndex(curX, curY)

            if (InteractionReachService.reachedObject(Position(curAbsX, curAbsY, z), worldObject)) {
                foundIndex = currentIndex
                foundStatus = ObjectRouteStatus.STRICT_REACHED
                break
            }
            if (curX == targetLocalX && curY == targetLocalY) {
                foundIndex = currentIndex
                foundStatus = ObjectRouteStatus.PARKED_CLOSEST
                break
            }

            val nextCost = cost[currentIndex] + 2
            for (step in OBJECT_ROUTE_STEPS) {
                val nextX = curX + step.dx
                val nextY = curY + step.dy
                if (nextX !in 0 until LOCAL_REGION_SIZE || nextY !in 0 until LOCAL_REGION_SIZE) {
                    continue
                }
                val nextIndex = localIndex(nextX, nextY)
                if (via[nextIndex] != 0 || !isObjectRouteStepTraversable(curAbsX, curAbsY, z, step.dx, step.dy)) {
                    continue
                }
                if (tail >= MAX_OBJECT_ROUTE_TILES) {
                    break
                }
                queueX[tail] = nextX
                queueY[tail] = nextY
                tail++
                via[nextIndex] = step.via
                cost[nextIndex] = nextCost
            }
        }

        if (foundIndex == -1) {
            foundIndex = findClosestFallbackIndex(via, cost, baseX, baseY, worldObject.x, worldObject.y, sizeX, sizeY)
            foundStatus = if (foundIndex == -1) ObjectRouteStatus.UNREACHABLE else ObjectRouteStatus.PARKED_CLOSEST
        }
        if (foundIndex == -1) {
            logger.info("[ObjRoute BFS] UNREACHABLE for obj=({},{}) from player=({},{})",
                worldObject.x, worldObject.y, follower.position.x, follower.position.y)
            return ObjectRouteResult(ObjectRouteStatus.UNREACHABLE)
        }

        val parkedAbsX = baseX + foundIndex % LOCAL_REGION_SIZE
        val parkedAbsY = baseY + foundIndex / LOCAL_REGION_SIZE
        val reachedFromParked = InteractionReachService.reachedObject(Position(parkedAbsX, parkedAbsY, z), worldObject)
        // If the fallback parked tile satisfies the reach check, treat it as STRICT_REACHED.
        // The BFS may have exited via tile-limit before dequeuing adjacent tiles (they get queued
        // late in BFS order), but findClosestFallbackIndex found the right tile — just upgrade
        // the status so the interaction is not cancelled.
        if (reachedFromParked && foundStatus == ObjectRouteStatus.PARKED_CLOSEST) {
            foundStatus = ObjectRouteStatus.STRICT_REACHED
        }
        logger.info(
            "[ObjRoute BFS] status={} target=({},{}) parked=({},{}) reachedFromParked={} obj=({},{}) player=({},{})",
            foundStatus, targetLocalX + baseX, targetLocalY + baseY,
            parkedAbsX, parkedAbsY, reachedFromParked,
            worldObject.x, worldObject.y, follower.position.x, follower.position.y,
        )

        return ObjectRouteResult(
            status = foundStatus,
            targetX = parkedAbsX,
            targetY = parkedAbsY,
            path = buildObjectRoutePath(foundIndex, sourceIndex, via, baseX, baseY, z),
        )
    }

    private fun isObjectRouteStepTraversable(x: Int, y: Int, z: Int, dx: Int, dy: Int): Boolean {
        if (!collisionManager.traversable(x + dx, y + dy, z, dx, dy)) {
            return false
        }
        if (dx != 0 && dy != 0) {
            return collisionManager.traversable(x + dx, y, z, dx, 0) &&
                collisionManager.traversable(x, y + dy, z, 0, dy)
        }
        return true
    }

    private fun findClosestFallbackIndex(
        via: IntArray,
        cost: IntArray,
        baseX: Int,
        baseY: Int,
        objX: Int,
        objY: Int,
        sizeX: Int,
        sizeY: Int,
    ): Int {
        var bestIndex = -1
        var bestDistance = 1_000
        var bestCost = 101
        val maxX = objX + sizeX - 1
        val maxY = objY + sizeY - 1
        for (x in (objX - FALLBACK_RADIUS)..(maxX + FALLBACK_RADIUS)) {
            for (y in (objY - FALLBACK_RADIUS)..(maxY + FALLBACK_RADIUS)) {
                val localX = x - baseX
                val localY = y - baseY
                if (localX !in 0 until LOCAL_REGION_SIZE || localY !in 0 until LOCAL_REGION_SIZE) {
                    continue
                }
                val index = localIndex(localX, localY)
                if (via[index] == 0 || cost[index] == 0 || cost[index] >= 100) {
                    continue
                }
                val distance = squaredDistanceToFootprint(x, y, objX, objY, maxX, maxY)
                if (distance < bestDistance || (distance == bestDistance && cost[index] < bestCost)) {
                    bestIndex = index
                    bestDistance = distance
                    bestCost = cost[index]
                }
            }
        }
        return bestIndex
    }

    private fun buildObjectRoutePath(
        destination: Int,
        source: Int,
        via: IntArray,
        baseX: Int,
        baseY: Int,
        z: Int,
    ): ArrayDeque<Node> {
        val reversed = ArrayDeque<Int>()
        var cursor = destination
        while (cursor != source && cursor >= 0) {
            reversed.addFirst(cursor)
            val flag = via[cursor]
            if (flag == 0 || flag == SOURCE_VIA) {
                return ArrayDeque()
            }
            val localX = cursor % LOCAL_REGION_SIZE
            val localY = cursor / LOCAL_REGION_SIZE
            var parentX = localX
            var parentY = localY
            if ((flag and VIA_WEST) != 0) {
                parentX += 1
            } else if ((flag and VIA_EAST) != 0) {
                parentX -= 1
            }
            if ((flag and VIA_SOUTH) != 0) {
                parentY += 1
            } else if ((flag and VIA_NORTH) != 0) {
                parentY -= 1
            }
            cursor = localIndex(parentX, parentY)
        }
        val path = ArrayDeque<Node>(reversed.size)
        var parent: Node? = null
        for (index in reversed) {
            val node = Node(baseX + index % LOCAL_REGION_SIZE, baseY + index / LOCAL_REGION_SIZE, z, 0, 0, parent)
            path.addLast(node)
            parent = node
        }
        return path
    }

    private fun findBestInside(srcX: Int, srcY: Int, objX: Int, objY: Int, sizeX: Int, sizeY: Int): Pair<Int, Int> {
        if (sizeX <= 1 || sizeY <= 1) {
            return objX to objY
        }
        var bestX = objX
        var bestY = objY
        var bestDistance = Int.MAX_VALUE
        for (x in objX until objX + sizeX) {
            val southDistance = abs(srcX - x) + abs(srcY - objY)
            if (southDistance < bestDistance) {
                bestDistance = southDistance
                bestX = x
                bestY = objY
            }
            val northY = objY + sizeY - 1
            val northDistance = abs(srcX - x) + abs(srcY - northY)
            if (northDistance < bestDistance) {
                bestDistance = northDistance
                bestX = x
                bestY = northY
            }
        }
        for (y in objY until objY + sizeY) {
            val westDistance = abs(srcX - objX) + abs(srcY - y)
            if (westDistance < bestDistance) {
                bestDistance = westDistance
                bestX = objX
                bestY = y
            }
            val eastX = objX + sizeX - 1
            val eastDistance = abs(srcX - eastX) + abs(srcY - y)
            if (eastDistance < bestDistance) {
                bestDistance = eastDistance
                bestX = eastX
                bestY = y
            }
        }
        // Return the actual closest footprint tile — matches tarnish Utility.findBestInside.
        // For a 1×1 source (all players), source.transform(bestX - srcX, bestY - srcY) = bestX, bestY.
        return bestX to bestY
    }

    private fun squaredDistanceToFootprint(x: Int, y: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): Int {
        val dx = when {
            x < minX -> minX - x
            x > maxX -> x - maxX
            else -> 0
        }
        val dy = when {
            y < minY -> minY - y
            y > maxY -> y - maxY
            else -> 0
        }
        return dx * dx + dy * dy
    }

    private fun localIndex(x: Int, y: Int): Int = y * LOCAL_REGION_SIZE + x

    private val CARDINAL_OFFSETS = listOf(-1 to 0, 1 to 0, 0 to 1, 0 to -1)
    private const val DEFAULT_OBJECT_TYPE = 10
    private const val LOCAL_REGION_SIZE = 104
    private const val MAX_OBJECT_ROUTE_TILES = 4_096
    private const val FALLBACK_RADIUS = 10
    private const val SOURCE_VIA = 99
    private const val VIA_SOUTH = 1
    private const val VIA_WEST = 2
    private const val VIA_NORTH = 4
    private const val VIA_EAST = 8
    private val OBJECT_ROUTE_STEPS =
        listOf(
            ObjectRouteStep(0, -1, VIA_SOUTH),
            ObjectRouteStep(-1, 0, VIA_WEST),
            ObjectRouteStep(0, 1, VIA_NORTH),
            ObjectRouteStep(1, 0, VIA_EAST),
            ObjectRouteStep(-1, -1, VIA_SOUTH or VIA_WEST),
            ObjectRouteStep(-1, 1, VIA_NORTH or VIA_WEST),
            ObjectRouteStep(1, -1, VIA_SOUTH or VIA_EAST),
            ObjectRouteStep(1, 1, VIA_NORTH or VIA_EAST),
        )
}

data class ObjectRouteResult(
    val status: ObjectRouteStatus,
    val targetX: Int? = null,
    val targetY: Int? = null,
    val path: ArrayDeque<Node> = ArrayDeque(),
)

enum class ObjectRouteStatus {
    ALREADY_REACHED,
    STRICT_REACHED,
    PARKED_CLOSEST,
    UNREACHABLE,
}

private data class ObjectRouteStep(val dx: Int, val dy: Int, val via: Int)
