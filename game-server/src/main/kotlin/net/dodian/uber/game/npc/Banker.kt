package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Banker : NpcScript("Banker", 1613, 1618, 3094, 7677) {
    override val definition = define {
        onOption("talk-to") {
            action {
                DialogueService.start(client) {
                    npcChat(npc.id, DialogueEmote.DEFAULT, "Good day, how can I help you?")
                    options(
                        title = "What would you like to say?",
                        DialogueOption("I'd like to access my bank account, please.") {
                            action { c -> openBank() }
                            finish()
                        },
                        DialogueOption("I'd like to check my PIN settings.") {
                            npcChat(npc.id, DialogueEmote.DEFAULT, "Pins have not been implemented yet.")
                            finish()
                        }
                    )
                }
                true
            }
        }

        onOption("bank") {
            action {
                openBank()
                true
            }
        }

        spawns(
            spawn(2727, 3378, face = NORTH),
            spawn(2615, 3094, face = WEST),
            spawn(2615, 3092, face = WEST),
            spawn(2615, 3091, face = WEST),
            spawn(2850, 2955, face = NORTH),
            spawn(2728, 3495, face = NORTH),
            spawn(2722, 3495, face = NORTH),
            spawn(2727, 3495, face = NORTH),
            spawn(2729, 3495, face = NORTH),
            spawn(2854, 2955, face = NORTH),
            spawn(2584, 3421, face = EAST),
            spawn(2584, 3419, face = EAST),
            spawn(2584, 3418, face = EAST),
            spawn(3252, 3418, face = NORTH),
            spawn(3253, 3418, face = NORTH),
            spawn(3254, 3418, face = NORTH),
            spawn(3187, 3436, face = NORTH),
            spawn(3187, 3438, face = NORTH),
            spawn(3187, 3440, face = NORTH),
            spawn(3187, 3442, face = NORTH),
            spawn(3187, 3444, face = NORTH),
            spawn(3187, 3446, face = NORTH),
            spawn(2807, 3443, face = SOUTH),
            spawn(2809, 3443, face = SOUTH),
            spawn(2810, 3443, face = SOUTH),
            spawn(2811, 3443, face = SOUTH),
            spawn(2724, 3495, face = NORTH),
            spawn(2721, 3495, face = NORTH),
            spawn(3096, 3489, face = WEST),
            spawn(3096, 3491, face = WEST),
            spawn(3096, 3492, face = NORTH),
            spawn(3098, 3492, face = NORTH),
            spawn(3054, 3381, face = WEST),
            spawn(2448, 3427, z = 1, face = NORTH),
            spawn(2448, 3424, z = 1, face = NORTH),
            spawn(2443, 3425, z = 1, face = NORTH),
            spawn(2443, 3424, z = 1, face = NORTH),
            spawn(2904, 3475, face = NORTH),
            spawn(2903, 3475, face = NORTH),
            spawn(2901, 3475, face = NORTH),
            spawn(2902, 3475, face = NORTH),
            spawn(2932, 4687, face = NORTH),
            spawn(3514, 3479, face = WEST),
            spawn(3259, 2780, face = NORTH),
            spawn(3514, 3481, face = WEST),
            spawn(3514, 3483, face = WEST),
            spawn(2869, 2983, z = 1, face = NORTH),
            // The 1618 spawn (originally 395)
            spawn(3174, 3028, face = NORTH).copy(npcId = 1618)
        )
    }
}
