package net.dodian.uber.game.engine.systems.interaction.npcs

import net.dodian.uber.game.engine.systems.follow.FollowRouting
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.utilities.Utils

object BankerApproachFallbackService {
    private val bankerNpcIds = setOf(394, 395, 1613, 7677)

    @JvmStatic
    fun shouldAttemptFallback(client: Client, npc: Npc, option: Int): Boolean {
        if (option !in 1..4) {
            return false
        }
        if (npc.id !in bankerNpcIds || client.position.z != npc.position.z) {
            return false
        }
        return true
    }

    @JvmStatic
    fun tryRouteCustomerSide(client: Client, npc: Npc): Boolean {
        val face = npc.face
        if (face !in Utils.directionDeltaX.indices) {
            return false
        }
        val dx = Utils.directionDeltaX[face].toInt()
        val dy = Utils.directionDeltaY[face].toInt()
        val tx = npc.position.x + dx * 2
        val ty = npc.position.y + dy * 2
        return FollowRouting.routeToExactTile(
            follower = client,
            destinationX = tx,
            destinationY = ty,
            z = npc.position.z,
            running = client.buttonOnRun,
        )
    }
}
