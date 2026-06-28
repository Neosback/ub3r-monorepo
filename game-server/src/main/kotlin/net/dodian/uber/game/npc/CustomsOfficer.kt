package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object CustomsOfficer : NpcModule {
    // Stats: 3648: r=0 a=0 d=0 s=0 hp=0 rg=0 mg=0

    val entries: List<NpcSpawnDef> = emptyList()

    val npcIds: IntArray = intArrayOf(3648)


    override val definition = legacyNpcDefinition(
        npcIds = npcIds,
        name = "CustomsOfficer",
        entries = entries,
        onFirstClick = ::onFirstClick,
        onSecondClick = ::onSecondClick,
    )

    fun onFirstClick(client: Client, npc: Npc): Boolean {
        DialogueService.start(client) {
            npcChat(npc.id, DialogueEmote.DEFAULT, "Hello dear.", "Would you like to travel?")
            options(
                title = "Do you wish to travel?",
                DialogueOption("Yes") {
                    action { c -> c.setTravelMenu() }
                    finish(closeInterfaces = false)
                },
                DialogueOption("No") {
                    playerChat(DialogueEmote.ANGRY1, "No thank you.")
                    finish()
                },
            )
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSecondClick(client: Client, npc: Npc): Boolean {
        client.setTravelMenu()
        return true
    }
}