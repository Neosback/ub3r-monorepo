package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object CustomsOfficier : NpcFamily by npcFamily("Customs officer", 3648, block = {
    options {
        talkTo(handler = ::handleCustomsOfficerTalkTo)
        third("pay-fare", ::handleCustomsOfficerPayFare)
    }

    spawns {
        spawn(2772, 3235, face = SOUTH)
        spawn(2804, 3421, face = WEST)
        spawn(2864, 2971, face = WEST)
        spawn(3274, 2797)
        spawn(3511, 3505)
    }
})

private fun handleCustomsOfficerTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "Hello dear.", "Would you like to travel?")
        options(
            title = "Do you wish to travel?",
            DialogueOption("Yes") {
                finishThen { it.setTravelMenu() }
            },
            DialogueOption("No") {
                playerChat(DialogueEmote.DEFAULT, "No thank you.")
                finish()
            },
        )
    }
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleCustomsOfficerPayFare(client: Client, npc: Npc): Boolean {
    client.setTravelMenu()
    return true
}
