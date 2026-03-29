package net.dodian.game.content.skills.mining

import net.dodian.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.content.skills.mining.MiningDefinitions
import net.dodian.game.content.skills.mining.MiningService

object MiningPlugin {
    @JvmStatic
    fun attempt(client: Client, objectId: Int, position: Position): Boolean {
        val rock = MiningDefinitions.rockByObjectId[objectId] ?: return false
        return MiningService.startMining(client, rock, position)
    }

    @JvmStatic
    fun stop(client: Client, fullReset: Boolean) = MiningService.stopMining(client, fullReset)
}
