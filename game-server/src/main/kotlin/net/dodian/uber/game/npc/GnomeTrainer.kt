package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.engine.systems.world.npc.NpcSpawnLocator
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object GnomeTrainer : NpcFamily by npcFamily("Gnome trainer", 6080, block = {
    definition {
        examine = "He can advise on training."
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleGnomeTrainerTalkTo)
    }

    spawns {
        spawn(2474, 3439)
        spawn(2547, 3554)
        spawn(3002, 3931)
        spawn(2475, 3428)
        spawn(2476, 3423, z = 1)
        spawn(2475, 3419, z = 2)
        spawn(2485, 3421, z = 2)
        spawn(2487, 3423)
        spawn(2486, 3430)
    }
})

private fun handleGnomeTrainerTalkTo(client: Client, npc: Npc): Boolean {
    if (NpcSpawnLocator.isGnomeCourseNpc(npc)) return false

    DialogueService.start(client) {
        npcChat(
            npc.id,
            DialogueEmote.DEFAULT,
            "Fancy meeting you here maggot.",
            "If you have any agility tickets,",
            "I would gladly take them from you.",
        )
        options(
            title = "Trade in tickets or teleport to agility course?",
            DialogueOption("Trade in tickets.") {
                finishThen { it.spendTickets() }
            },
            DialogueOption("Another course, please.") {
                val course = trainerCourse(npc)
                options(
                    title = "Which course do you wish to be taken to?",
                    DialogueOption(if (course == 1) "Barbarian" else "Gnome") {
                        finishThen { player ->
                            if (course == 1) player.teleportTo(2547, 3553, 0)
                            else player.teleportTo(2474, 3438, 0)
                        }
                    },
                    DialogueOption(if (course == 3) "Barbarian" else "Wilderness") {
                        finishThen { player ->
                            if (course == 3) player.teleportTo(2547, 3553, 0)
                            else player.teleportTo(3002, 3932, 0)
                        }
                    },
                    DialogueOption("Stay here") { finish() },
                )
            },
        )
    }
    return true
}

private fun trainerCourse(npc: Npc): Int = when {
    npc.position.x == 3002 && npc.position.y == 3931 -> 3
    npc.position.x == 2547 && npc.position.y == 3554 -> 2
    else -> 1
}
