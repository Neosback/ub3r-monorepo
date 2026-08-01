package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object ArmourSalesman : NpcFamily by npcFamily("Armour salesman", 6059, block = {
    definition {
        examine = "Supplier of Rangers armour."
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleArmourSalesmanTalkTo)
        third("trade", ::handleArmourSalesmanTrade)
    }

    spawns {
        spawn(2725, 3369)
    }
})

private fun handleArmourSalesmanTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "Would you like to see my capes?")
        options(
            title = "Select an Option",
            DialogueOption("Yes, please.") {
                finishThen { it.openUpShopRouted(ShopId.CAPE_SHOP.id) }
            },
            DialogueOption("No, thank you.") {
                playerChat(DialogueEmote.DEFAULT, "No, thank you.")
                finish()
            },
        )
    }
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleArmourSalesmanTrade(client: Client, npc: Npc): Boolean {
    client.openUpShopRouted(ShopId.CAPE_SHOP.id)
    return true
}
