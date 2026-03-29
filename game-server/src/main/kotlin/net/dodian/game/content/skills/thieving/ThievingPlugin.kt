package net.dodian.game.content.skills.thieving

import net.dodian.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.game.content.skills.thieving.ThievingService

object ThievingPlugin {
    @JvmStatic
    fun attempt(client: Client, entityId: Int, position: Position) = ThievingService.attempt(client, entityId, position)
}
