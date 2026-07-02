package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Vannaka : NpcFamily by npcFamily("Vannaka", 403, block = {
    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleSlayerMasterTalkTo)
        third("assignment", ::handleSlayerMasterAssignment)
    }

    spawns {
        spawn(2798, 3441)
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
