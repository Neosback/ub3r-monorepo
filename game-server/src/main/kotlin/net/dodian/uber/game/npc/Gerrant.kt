package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object Gerrant : NpcFamily by npcFamily("Gerrant", 1790, block = {
    cache {
        examine = "A Fishing expert."
        name = "Gerrant"
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleGerrantTrade)
        third("trade", ::handleGerrantTrade)
    }

    spawns {
        spawn(2597, 3401)
        spawn(2835, 3442)
        spawn(2871, 2968)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleGerrantTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.FISHING_SUPPLIES.id)
    return true
}
