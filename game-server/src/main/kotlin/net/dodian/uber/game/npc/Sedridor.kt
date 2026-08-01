package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.objects.travel.EssenceMineTravel

internal object Sedridor : NpcFamily by npcFamily("Archmage Sedridor", 11433, block = {
    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleSedridorTalkTo)
        third("teleport", ::handleSedridorTeleport)
    }

    spawns {
        spawn(2590, 3086, face = EAST)
    }
})

private fun handleSedridorTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "I can teleport you to the essence mine.")
        options(
            title = "Teleport to the essence mine?",
            DialogueOption("Yes please.") {
                finishThen(action = EssenceMineTravel::sendToEssenceMine)
            },
            DialogueOption("No thanks.") {
                playerChat(DialogueEmote.DEFAULT, "No thanks.")
            },
        )
    }
    return true
}

@Suppress("UNUSED_PARAMETER")
private fun handleSedridorTeleport(client: Client, npc: Npc): Boolean {
    EssenceMineTravel.sendToEssenceMine(client)
    return true
}
