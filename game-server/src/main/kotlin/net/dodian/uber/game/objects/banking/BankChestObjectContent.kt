package net.dodian.uber.game.objects.banking

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client

import net.dodian.uber.game.api.interaction.ObjectInteractionContext

object BankChestObjectContent : ObjectContent {
    override val objectIds: IntArray = BankingObjectIds.chestObjects

    // No clickInteractionPolicy override: falls through to ObjectInteractionPolicy.DEFAULT (REACHABLE),
    // the generic footprint+type+rotation reach in InteractionReachService — same as every other
    // walk-up-and-use object. The object's real type/rotation now comes from the cache (Geometry.getObject).

    override fun onFirstClick(context: ObjectInteractionContext): Boolean {
        context.player.openUpBankRouted()
        return true
    }

    override fun onSecondClick(context: ObjectInteractionContext): Boolean {
        if (context.objectId == 6943 || context.objectId == 6948) {
            context.player.openUpBankRouted()
        }
        return true
    }
}
