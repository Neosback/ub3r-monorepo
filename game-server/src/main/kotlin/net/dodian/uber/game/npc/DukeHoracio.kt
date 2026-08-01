package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object DukeHoracio : NpcFamily by npcFamily("Duke Horacio", 8051, block = {
    definition {
        examine = "Duke Horacio of Lumbridge."
        name = "Duke Horacio"
    }

    options {
        talkTo(handler = ::handleDukeHoracioTalkTo)
    }
})

private fun handleDukeHoracioTalkTo(client: Client, npc: Npc): Boolean {
    DialogueService.start(client) {
        npcChat(npc.id, DialogueEmote.DEFAULT, "Happy Holidays adventurer!")
        npcChat(
            npc.id,
            DialogueEmote.DEFAULT,
            "The monsters are trying to ruin the new year!",
            "You must slay them to take back your gifts and",
            "save the spirit of 2021!"
        )
        options(
            title = "Select an Option",
            DialogueOption("I'd like to see your shop.") {
                finish()
            },
            DialogueOption("I'll just be on my way.") {
                finish()
            }
        )
    }
    return true
}
