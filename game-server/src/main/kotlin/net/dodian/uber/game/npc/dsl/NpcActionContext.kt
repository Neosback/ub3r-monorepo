package net.dodian.uber.game.npc.dsl

import net.dodian.uber.game.activity.partyroom.PartyRoomBalloons
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.engine.systems.interaction.npcs.NpcInteractionActionService
import net.dodian.uber.game.shop.ShopId

class NpcActionContext(
    val client: Client,
    val npc: Npc,
    val option: String
) {
    val profile: String?
        get() = npc.interactionProfile

    fun openShop(shopId: ShopId) {
        NpcInteractionActionService.openShop(client, shopId.id)
    }

    fun openShop(shopId: Int) {
        NpcInteractionActionService.openShop(client, shopId)
    }

    fun openBank() {
        NpcInteractionActionService.openBank(client)
    }

    fun teleport(
        position: Position,
        random: Int = 0,
        message: String? = null
    ) {
        val rx = position.x + randomOffset(random)
        val ry = position.y + randomOffset(random)
        NpcInteractionActionService.teleport(client, rx, ry, position.z, message)
    }

    fun balloonsEventActive(): Boolean {
        return PartyRoomBalloons.isPartyEventActive()
    }

    private fun randomOffset(range: Int): Int {
        if (range <= 0) return 0
        val width = range * 2 + 1
        return (Math.random() * width).toInt() - range
    }
}

typealias NpcAction = NpcActionContext.() -> Unit
