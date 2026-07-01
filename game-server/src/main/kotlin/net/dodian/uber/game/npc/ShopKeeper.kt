package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object ShopKeeper : NpcFamily by npcFamily("Shop keeper", 555, block = {
    ids(2813)

    cache {
        name = "Shop keeper"
    }

    cache(2813) {
        name = "Shop keeper"
    }

    runtime {
        deathAnimation = 2304
    }

    runtime(2813) {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ShopKeeper::openGeneralStore)
        trade(handler = ShopKeeper::openGeneralStore)
    }

    spawns {
        spawn(2604, 3092)
        spawnId(2813, 2595, 3104)
        spawnId(2813, 3216, 3416)
    }
}) {

    fun openGeneralStore(client: Client, npc: Npc): Boolean {
        client.openUpShopRouted(ShopId.GENERAL_STORE.id)
        return true
    }
}
