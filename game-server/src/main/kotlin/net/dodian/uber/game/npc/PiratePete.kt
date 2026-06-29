package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object PiratePete : NpcScript("PiratePete", 2825) {
    override val definition = define {
        stats {
            hitpoints = 0
            attack = 0
            defence = 0
            strength = 0
            magic = 0
            ranged = 0
            attackAnimation = 806
            deathAnimation = 2304
            respawnTicks = 60
        }

        onOption("talk-to") {
            action {
                DialogueService.start(client) {
                    npcChat(npc.id, DialogueEmote.DEFAULT, "Cant talk right now!")
                    finish()
                }
                true
            }
        }
    }
}