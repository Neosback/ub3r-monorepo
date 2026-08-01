package net.dodian.uber.game.activity.partyroom

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.objects.ObjectContent
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.player.Client

import net.dodian.uber.game.api.interaction.ObjectInteractionContext

object PartyRoomObjectContent : ObjectContent {
    override val objectIds: IntArray = PartyRoomObjectIds.allObjectIds

    override fun onFirstClick(context: ObjectInteractionContext): Boolean {
        val client = context.player
        val objectId = context.objectId
        val position = context.position
        if (objectId in PartyRoomObjectIds.balloonObjectIds && PartyRoomBalloons.lootBalloon(client, position.copy())) {
            return true
        }
        if (objectId == PartyRoomObjectIds.DEPOSIT_CHEST) {
            PartyRoomBalloons.openPartyRoomInterface(client)
            return true
        }
        if (objectId == PartyRoomObjectIds.FORCE_TRIGGER && client.playerRights > 1) {
            PartyRoomBalloons.triggerPartyEvent(client)
            return true
        }
        return false
    }
}
