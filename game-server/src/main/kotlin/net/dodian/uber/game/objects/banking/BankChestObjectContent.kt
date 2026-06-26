package net.dodian.uber.game.objects.banking

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client

object BankChestObjectContent : ObjectContent {
    override val objectIds: IntArray = BankingObjectIds.chestObjects

    // No clickInteractionPolicy override: falls through to ObjectInteractionPolicy.DEFAULT (REACHABLE),
    // the generic footprint+type+rotation reach in InteractionReachService — same as every other
    // walk-up-and-use object. The object's real type/rotation now comes from the cache (Geometry.getObject).

    override fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        client.openUpBankRouted()
        return true
    }

    override fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        if (objectId == 6943 || objectId == 6948) {
            client.openUpBankRouted()
        }
        return true
    }
}
