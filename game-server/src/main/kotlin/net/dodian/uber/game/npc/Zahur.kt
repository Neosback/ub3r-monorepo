package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.shop.ShopId

internal object Zahur : NpcFamily by npcFamily("Zahur", 4753, block = {
    cache {
        examine = "Like to mix with herblore"
    }

    options {
        talkTo(handler = ::handleZahurTalkTo)
        third("decant", ::handleZahurDecant)
        fourth("clean", ::handleZahurCleanHerbs)
    }

    spawns {
        spawn(3424, 2908)
    }
})

private fun handleZahurTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(
            npc.id,
            DialogueEmote.DEFAULT,
            "Hello ${if (client.gender == 1) "miss" else "mr"} adventurer.",
            "What can I help you with today?",
        )
        playerChat(
            DialogueEmote.DEFAULT,
            "I heard you were a famous herbalist.",
            "I was wondering if you had some kind of service.",
        )
        npcChat(
            npc.id,
            DialogueEmote.DEFAULT,
            "I sure do have some services I can offer.",
            "Would you like me to make you a unfinish potion or",
            "Clean any of your herbs? They must all be noted.",
        )
        npcChat(
            npc.id,
            DialogueEmote.DEFAULT,
            "This service will cost you 200 coins per herb",
            "and 1000 coins per potion.",
            "I also got a nice store if you wish to take a look.",
        )
        options(
            title = "Select a option",
            DialogueOption("Visit the store") {
                finishThen {
                    it.openUpShopRouted(ShopId.JATIX_HERBLORE_STORE.id)
                }
            },
            DialogueOption("Clean herbs") {
                finishThen {
                    HerbloreNpcDialogue.openHerbCleaner(it, npc.id)
                }
            },
            DialogueOption("Make unfinish potions") {
                finishThen {
                    HerbloreNpcDialogue.openUnfinishedPotionMaker(it, npc.id)
                }
            },
        )
    }
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleZahurDecant(client: Client, npc: Npc): Boolean {
    HerbloreNpcDialogue.openDecantDoseOptions(client, npc.id)
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleZahurCleanHerbs(client: Client, npc: Npc): Boolean {
    HerbloreNpcDialogue.openHerbCleaner(client, npc.id)
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleZahurMakeUnfinishedPotions(client: Client, npc: Npc): Boolean {
    HerbloreNpcDialogue.openUnfinishedPotionMaker(client, npc.id)
    return true
}
