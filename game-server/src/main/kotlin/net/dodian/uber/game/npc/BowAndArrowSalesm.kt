package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object BowAndArrowSalesm : NpcFamily by npcFamily("Bow and Arrow salesman", 6060, block = {
    definition {
        examine = "Supplier of Archery equipment."
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleBowAndArrowSalesmanTrade)
        third("trade", ::handleBowAndArrowSalesmanTrade)
    }

    spawns {
        spawn(2589, 3083)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleBowAndArrowSalesmanTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.RANGE_STORE.id)
    return true
}
