package net.dodian.game.content.skills.mining.objects

import net.dodian.cache.`object`.GameObjectData
import net.dodian.game.content.objects.ObjectContent
import net.dodian.game.model.Position
import net.dodian.uber.game.model.entity.player.Client

object SpecialMiningObjectBindings : ObjectContent {
    override val objectIds: IntArray = SpecialMiningObjectComponents.objectIds

    override fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean = false
}
