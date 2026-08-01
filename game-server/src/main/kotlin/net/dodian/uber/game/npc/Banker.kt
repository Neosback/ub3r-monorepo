package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Banker : NpcFamily by npcFamily("Banker", 1613, block = {
    definition {
        examine = "I do not get paid enough for this."
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleBankerTalkTo)
        third("bank", ::handleBankerBank)
    }

    spawns {
        spawn(2443, 3424, z = 1)
        spawn(2443, 3425, z = 1)
        spawn(2448, 3424, z = 1)
        spawn(2448, 3427, z = 1)
        spawn(2584, 3421, face = EAST)
        spawn(2615, 3091, face = WEST)
        spawn(2615, 3092, face = WEST)
        spawn(2615, 3094, face = WEST)
        spawn(2721, 3495, face = SOUTH)
        spawn(2722, 3495, face = SOUTH)
        spawn(2724, 3495, face = SOUTH)
        spawn(2727, 3378, face = SOUTH)
        spawn(2727, 3495, face = SOUTH)
        spawn(2728, 3495, face = SOUTH)
        spawn(2729, 3495, face = SOUTH)
        spawn(2807, 3443, face = SOUTH)
        spawn(2809, 3443, face = SOUTH)
        spawn(2810, 3443, face = SOUTH)
        spawn(2811, 3443, face = SOUTH)
        spawn(2850, 2955)
        spawn(2854, 2955)
        spawn(2869, 2983, z = 1)
        spawn(2901, 3475)
        spawn(2902, 3475)
        spawn(2903, 3475)
        spawn(2904, 3475)
        spawn(2932, 4687)
        spawn(3054, 3381, face = WEST)
        spawn(3096, 3489, face = WEST)
        spawn(3096, 3491, face = WEST)
        spawn(3096, 3492)
        spawn(3098, 3492)
        spawn(3187, 3436)
        spawn(3187, 3438)
        spawn(3187, 3440)
        spawn(3187, 3442)
        spawn(3187, 3444)
        spawn(3187, 3446)
        spawn(3252, 3418)
        spawn(3253, 3418)
        spawn(3254, 3418)
        spawn(3259, 2780)
        spawn(3514, 3479, face = WEST)
        spawn(3514, 3481, face = WEST)
        spawn(3514, 3483, face = WEST)
    }
})

private fun handleBankerTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "Good day. How can I help you?")
        options(
            title = "Select an Option",
            DialogueOption("I'd like to access my bank account, please.") {
                finishThen { it.openUpBankRouted() }
            },
            DialogueOption("I'd like to check my PIN settings.") {
                finishThen { it.openUpBankRouted() }
            },
            DialogueOption("I'd like to collect items.") {
                finishThen { it.openUpBankRouted() }
            },
            DialogueOption("Actually, I don't need anything.") {
                npcChat(npc.id, DialogueEmote.DEFAULT, "Well, come back if you change your mind.")
                finish()
            },
        )
    }
    return true
}

private fun handleBankerBank(client: Client, npc: Npc): Boolean {
    client.openUpBankRouted()
    return true
}
