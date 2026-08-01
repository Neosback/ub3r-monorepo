package net.dodian.uber.game.objects.travel

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.model.objects.GlobalObject
import net.dodian.uber.game.model.objects.WorldObject
import net.dodian.uber.game.api.content.ContentInteraction
import net.dodian.uber.game.engine.util.Misc

import net.dodian.uber.game.api.interaction.ObjectInteractionContext

object WebObstacleObjectContent : ObjectContent {
    override val objectIds: IntArray = TravelObjectIds.webObstacleObjects

    override fun onFirstClick(context: ObjectInteractionContext): Boolean {
        val client = context.player
        val objectId = context.objectId
        val position = context.position
        if (Misc.chance(100) <= 50) {
            client.sendMessage("You failed to cut the web!")
            return true
        }
        if (!ContentInteraction.tryAcquireMs(client, ContentInteraction.WEB_OBSTACLE, 2000L)) {
            return true
        }
        val emptyObj = WorldObject(734, position.x, position.y, client.position.z, 10, 1, objectId)
        if (!GlobalObject.addGlobalObject(emptyObj, 30000)) {
            return true
        }
        return true
    }
}
