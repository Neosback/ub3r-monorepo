package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.dialogue.DialogueEmote
import net.dodian.uber.game.api.content.dialogue.DialogueOption
import net.dodian.uber.game.engine.systems.dialogue.DialogueService
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.SetTabInterface

internal object MakeoverMage : NpcModule {
    val entries: List<NpcSpawnDef> = emptyList()
    val npcIds: IntArray = intArrayOf(1306)


    override val definition = legacyNpcDefinition(
        npcIds = npcIds,
        name = "MakeoverMage",
        entries = entries,
        onFirstClick = ::onFirstClick,
        onThirdClick = ::onThirdClick,
    )

    fun onFirstClick(client: Client, npc: Npc): Boolean {
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
                    action { c -> c.send(SetTabInterface(3559, 3213)) }
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

    @Suppress("UNUSED_PARAMETER")
    fun onThirdClick(client: Client, npc: Npc): Boolean {
        client.send(SetTabInterface(3559, 3213))
        return true
    }
}