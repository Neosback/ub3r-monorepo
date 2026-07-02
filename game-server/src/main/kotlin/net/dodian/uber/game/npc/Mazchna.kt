package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Mazchna : NpcFamily by npcFamily("Mazchna", 402, block = {
    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleSlayerMasterTalkTo)
        third("assignment", ::handleSlayerMasterAssignment)
    }

    spawns {
        spawn(2885, 3450)
    }
})

private fun handleSlayerMasterTalkTo(client: Client, npc: Npc): Boolean {
    SlayerMasterDialogue.startIntro(client, npc.id)
    return true
}

private fun handleSlayerMasterAssignment(client: Client, npc: Npc): Boolean {
    SlayerMasterDialogue.assignTask(client, npc.id)
    return true
}
