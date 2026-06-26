package net.dodian.uber.game.engine.systems.pathing

import java.util.ArrayDeque
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

/** Deterministic Tarnish-style weighted traversal search (10 cardinal / 14 diagonal). */
class DijkstraPathfindingAlgorithm(
    private val collision: PathingCollision,
) : PathfindingAlgorithm {
    override fun find(srcX: Int, srcY: Int, dstX: Int, dstY: Int, z: Int): ArrayDeque<Node> {
        if (srcX == dstX && srcY == dstY) return ArrayDeque()
        val minX = min(srcX, dstX) - SEARCH_MARGIN
        val minY = min(srcY, dstY) - SEARCH_MARGIN
        val maxX = max(srcX, dstX) + SEARCH_MARGIN
        val maxY = max(srcY, dstY) + SEARCH_MARGIN
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        val total = width * height
        val distances = IntArray(total) { Int.MAX_VALUE }
        val parents = IntArray(total) { -1 }
        val source = index(srcX, srcY, minX, minY, width)
        val destination = index(dstX, dstY, minX, minY, width)
        val open = PriorityQueue(compareBy<Cell>({ it.cost }, { it.index }))
        distances[source] = 0
        open += Cell(source, 0)
        var expansions = 0

        while (open.isNotEmpty() && expansions++ < MAX_EXPANSIONS) {
            val current = open.remove()
            if (current.cost != distances[current.index]) continue
            if (current.index == destination) {
                return buildPath(source, destination, parents, distances, minX, minY, width, z)
            }
            val x = minX + current.index % width
            val y = minY + current.index / width
            for (dx in -1..1) for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nextX = x + dx
                val nextY = y + dy
                if (nextX !in minX..maxX || nextY !in minY..maxY || !collision.traversable(nextX, nextY, z, dx, dy)) continue
                val next = index(nextX, nextY, minX, minY, width)
                val cost = current.cost + if (dx == 0 || dy == 0) 10 else 14
                if (cost >= distances[next]) continue
                distances[next] = cost
                parents[next] = current.index
                open += Cell(next, cost)
            }
        }
        // Fallback: destination is unreachable, find the closest reachable tile
        var bestIndex = -1
        var bestDistSq = 1000
        var bestCost = Int.MAX_VALUE
        val radius = 10

        for (x in (dstX - radius)..(dstX + radius)) {
            for (y in (dstY - radius)..(dstY + radius)) {
                if (x in minX..maxX && y in minY..maxY) {
                    val idx = index(x, y, minX, minY, width)
                    val cost = distances[idx]
                    if (cost != Int.MAX_VALUE) {
                        val dx = x - dstX
                        val dy = y - dstY
                        val distSq = dx * dx + dy * dy
                        if (distSq < bestDistSq || (distSq == bestDistSq && cost < bestCost)) {
                            bestDistSq = distSq
                            bestCost = cost
                            bestIndex = idx
                        }
                    }
                }
            }
        }

        if (bestIndex != -1 && bestDistSq < 1000) {
            return buildPath(source, bestIndex, parents, distances, minX, minY, width, z)
        }

        return ArrayDeque()
    }

    private fun buildPath(source: Int, destination: Int, parents: IntArray, distances: IntArray,
                          minX: Int, minY: Int, width: Int, z: Int): ArrayDeque<Node> {
        val reversed = ArrayDeque<Int>()
        var cursor = destination
        while (cursor != source && cursor >= 0) {
            reversed.addFirst(cursor)
            cursor = parents[cursor]
        }
        if (cursor != source) return ArrayDeque()
        val result = ArrayDeque<Node>(reversed.size)
        var parent: Node? = null
        for (cell in reversed) {
            val node = Node(minX + cell % width, minY + cell / width, z, distances[cell], 0, parent)
            result.addLast(node)
            parent = node
        }
        return result
    }

    private fun index(x: Int, y: Int, minX: Int, minY: Int, width: Int) = (y - minY) * width + x - minX
    private data class Cell(val index: Int, val cost: Int)

    companion object {
        private const val SEARCH_MARGIN = 32
        private const val MAX_EXPANSIONS = 16_384
    }
}