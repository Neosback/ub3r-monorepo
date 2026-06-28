package net.dodian.uber.game.engine.systems.net

import kotlin.math.hypot
import net.dodian.uber.game.engine.systems.pathing.DijkstraPathfindingAlgorithm
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionManager
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.entity.player.Player

object WalkingRouteService {
    private val pathfinder =
        DijkstraPathfindingAlgorithm { x, y, z, dx, dy ->
            CollisionManager.global().traversable(x, y, z, dx, dy)
        }

    fun routePlainWalk(player: Client, request: WalkRequest): Boolean {
        val destination = destination(request, player.position.z)
        if (hypot((player.position.x - destination.x).toDouble(), (player.position.y - destination.y).toDouble()) > MAX_CLICK_DISTANCE) {
            player.resetWalkingQueue()
            return false
        }

        val path = pathfinder.find(player.position.x, player.position.y, destination.x, destination.y, player.position.z)
        if (path.isEmpty()) {
            player.resetWalkingQueue()
            return false
        }

        val baseX = player.mapRegionX * 8
        val baseY = player.mapRegionY * 8
        val steps = minOf(path.size, Player.WALKING_QUEUE_SIZE)
        player.newWalkCmdSteps = steps
        player.newWalkCmdIsRunning = request.running

        var index = 0
        for (node in path) {
            if (index >= steps) {
                break
            }
            player.newWalkCmdX[index] = node.x - baseX
            player.newWalkCmdY[index] = node.y - baseY
            player.tmpNWCX[index] = player.newWalkCmdX[index]
            player.tmpNWCY[index] = player.newWalkCmdY[index]
            index++
        }
        return true
    }

    fun destination(request: WalkRequest, z: Int): Position {
        val last = request.deltasX.lastIndex
        val x = request.firstStepXAbs + if (last >= 0) request.deltasX[last] else 0
        val y = request.firstStepYAbs + if (last >= 0) request.deltasY[last] else 0
        return Position(x, y, z)
    }

    fun isPlainWalkOpcode(opcode: Int): Boolean = opcode == 164 || opcode == 248

    private const val MAX_CLICK_DISTANCE = 32.0
}
