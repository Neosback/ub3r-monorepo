package net.dodian.uber.game.objects.banking

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client

object BankBoothObjectContent : ObjectContent {
    override val objectIds: IntArray = BankingObjectIds.boothObjects

    // No clickInteractionPolicy override: falls through to ObjectInteractionPolicy.DEFAULT (REACHABLE),
    // the generic footprint+type+rotation reach in InteractionReachService — same as every other
    // walk-up-and-use object. The object's real type/rotation now comes from the cache (Geometry.getObject).

    override fun onFirstClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        client.openUpBankRouted()
        return true
    }

    override fun onSecondClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        client.openUpBankRouted()
        return true
    }

    override fun onThirdClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        client.setRefundList()
        client.refundSlot = 0
        client.setRefundOptions()
        return true
    }

    override fun onFourthClick(client: Client, objectId: Int, position: Position, obj: GameObjectData?): Boolean {
        client.sendMessage("This bank options are not working currently!")
        return true
    }
}
