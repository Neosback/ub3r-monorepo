package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Monk : NpcModule {
    val entries: List<NpcSpawnDef> = emptyList()
    val npcIds: IntArray = intArrayOf(555)


    override val definition = legacyNpcDefinition(
        npcIds = npcIds,
        name = "Monk",
        entries = entries,
        onFirstClick = ::onFirstClick,
    )

    fun onFirstClick(client: Client, npc: Npc): Boolean {
        client.quests[0]++
        client.sendMessage(
            if (client.playerRights > 1) {
                "Set your quest to: ${client.quests[0]}"
            } else {
                "Suddenly the monk had an urge to dissapear!"
            },
        )
        return true
    }
}