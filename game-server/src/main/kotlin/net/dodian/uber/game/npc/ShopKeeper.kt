package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object ShopKeeper : NpcFamily by npcFamily("Shop keeper", 2813, block = {
    definition(2813) {
        name = "Shop keeper"
    }

    server(2813) {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleShopKeeperTalkTo)
        third("trade", ::handleShopKeeperTrade)
    }

    spawns {
        spawn(2595, 3104, walkRadius = 3)
        spawn(3216, 3416, walkRadius = 3)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleShopKeeperTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "Hello. Would you like to buy anything?")
        options(
            title = "Open the general store?",
            DialogueOption("Yes please.") {
                finishThen {
                    it.openUpShopRouted(ShopId.GENERAL_STORE.id)
                }
            },
            DialogueOption("No thanks.") {
                playerChat(DialogueEmote.DEFAULT, "No thanks.")
                finish()
            },
        )
    }
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleShopKeeperTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.GENERAL_STORE.id)
    return true
}
