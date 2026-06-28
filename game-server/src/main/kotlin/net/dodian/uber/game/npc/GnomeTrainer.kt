package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.engine.systems.world.npc.NpcSpawnLocator

internal object GnomeTrainer : NpcModule {
    // Stats: 6080: r=60 a=0 d=0 s=0 hp=0 rg=0 mg=0

    val entries: List<NpcSpawnDef> = emptyList()

    val npcIds: IntArray = intArrayOf(6080)


    override val definition = legacyNpcDefinition(
        npcIds = npcIds,
        name = "GnomeTrainer",
        entries = entries,
        onFirstClick = ::onFirstClick,
    )

    fun onFirstClick(client: Client, npc: Npc): Boolean {
        if (NpcSpawnLocator.isGnomeCourseNpc(npc)) {
            return false
        }
        DialogueService.start(client) {
            npcChat(npc.id, DialogueEmote.DEFAULT, "Fancy meeting you here maggot.", "If you have any agility ticket,", "I would gladly take them from you.")
            options(
                title = "Trade in tickets or teleport to agility course?",
                DialogueOption("Trade in tickets.") {
                    action { c -> c.spendTickets() }
                    finish()
                },
                DialogueOption("Another course, please.") {
                    val atWilderness = npc.position.x == 3002 && npc.position.y == 3931
                    val atBarbarian = npc.position.x == 2547 && npc.position.y == 3554
                    val courseOptions = if (atWilderness) {
                        arrayOf("Gnome", "Barbarian", "Stay here")
                    } else if (atBarbarian) {
                        arrayOf("Gnome", "Wilderness", "Stay here")
                    } else {
                        arrayOf("Barbarian", "Wilderness", "Stay here")
                    }
                    options(
                        title = "Which course do you wish to be taken to?",
                        DialogueOption(courseOptions[0]) {
                            action { c ->
                                if (atWilderness) c.teleportTo(2474, 3438, 0)
                                else if (atBarbarian) c.teleportTo(2474, 3438, 0)
                                else c.teleportTo(2547, 3553, 0)
                            }
                            finish()
                        },
                        DialogueOption(courseOptions[1]) {
                            action { c ->
                                if (atWilderness) c.teleportTo(2547, 3553, 0)
                                else if (atBarbarian) c.teleportTo(3002, 3932, 0)
                                else c.teleportTo(3002, 3932, 0)
                            }
                            finish()
                        },
                        DialogueOption(courseOptions[2]) { finish() },
                    )
                },
            )
        }
        return true
    }
}