package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.SetTabInterface

internal object MakeoverMage : NpcFamily by npcFamily("Make-over mage", 1306, block = {
    ids(1307)

    cache {
        name = "Make-over mage"
    }

    runtime {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = MakeoverMage::talkTo)
        third("makeover", MakeoverMage::openMakeover)
    }

    spawns {
        spawn(2603, 3088)
    }
}) {

    fun talkTo(client: Client, npc: Npc): Boolean {
        DialogueService.start(client) {
            npcChat(
                npc.id,
                DialogueEmote.DEFAULT,
                "Hello there, would you like to change your looks? If so, it will be free of charge.",
            )
            options(
                title = "Would you like to change your looks?",
                DialogueOption("Sure") {
                    playerChat(DialogueEmote.DEFAULT, "I would love that.")
                    action { it.send(SetTabInterface(3559, 3213)) }
                    finish(closeInterfaces = false)
                },
                DialogueOption("No thanks") {
                    playerChat(DialogueEmote.DEFAULT, "Not at the moment.")
                    finish()
                },
            )
        }
        return true
    }

    fun openMakeover(client: Client, npc: Npc): Boolean {
        client.send(SetTabInterface(3559, 3213))
        return true
    }
}
