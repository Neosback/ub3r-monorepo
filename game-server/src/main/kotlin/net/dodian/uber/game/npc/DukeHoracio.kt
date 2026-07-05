package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.dialogue.DialogueIds
import net.dodian.uber.game.engine.systems.dialogue.core.DialogueRegistry
import net.dodian.uber.game.ui.dialogue.DialogueUi
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object DukeHoracio : NpcFamily by npcFamily("Duke Horacio", 8051, block = {
    cache {
        examine = "Duke Horacio of Lumbridge."
        name = "Duke Horacio"
    }

    options {
        talkTo(handler = ::handleDukeHoracioTalkTo)
    }
}) {
    fun registerLegacyDialogues(builder: DialogueRegistry.Builder) {
        builder.handle(DialogueIds.Misc.HOLIDAY_GREETING) { c ->
            c.showNPCChat(c.NpcTalkTo, 591, arrayOf("Happy Holidays adventurer!"))
            c.nextDiag = DialogueIds.Misc.HOLIDAY_INFO
            c.NpcDialogueSend = true
            true
        }

        builder.handle(DialogueIds.Misc.HOLIDAY_INFO) { c ->
            c.showNPCChat(c.NpcTalkTo, 591, arrayOf("The monsters are trying to ruin the new year!", "You must slay them to take back your gifts and", "save the spirit of 2021!"))
            c.nextDiag = DialogueIds.Misc.HOLIDAY_OPTIONS
            c.NpcDialogueSend = true
            true
        }

        builder.handle(DialogueIds.Misc.HOLIDAY_OPTIONS) { c ->
            DialogueUi.showPlayerOption(c, arrayOf("Select a option", "I'd like to see your shop.", "I'll just be on my way."))
            c.NpcDialogueSend = true
            true
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun handleDukeHoracioTalkTo(client: Client, npc: Npc): Boolean {
    client.NpcWanneTalk = 8051
    return true
}
