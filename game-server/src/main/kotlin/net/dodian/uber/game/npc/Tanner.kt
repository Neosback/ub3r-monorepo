package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object Tanner : NpcFamily by npcFamily("Tanner", 5809, block = {
    cache {
        firstAction("Tan")
        thirdAction("Trade")
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleTannerTan)
        third("trade", ::handleTannerTrade)
    }

    spawns {
        spawn(2711, 3478)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleTannerTan(client: Client, npc: Npc): Boolean {
    client.openTan()
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleTannerTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.CRAFTING_STORE.id)
    return true
}
