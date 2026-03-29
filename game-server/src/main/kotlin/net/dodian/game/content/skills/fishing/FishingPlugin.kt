package net.dodian.game.content.skills.fishing

import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.content.skills.fishing.FishingService

object FishingPlugin {
    @JvmStatic
    fun attempt(client: Client, objectId: Int, clickOption: Int) = FishingService.start(client, objectId, clickOption)
}
