package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.objects.travel.LegendsGuildGateService

internal object LegendsGuard : NpcFamily by npcFamily("Legends' Guard", 3951, block = {
    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleLegendsGuardTalkTo)
    }

    spawns {
        spawn(2727, 3349)
        spawn(2730, 3349)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleLegendsGuardTalkTo(client: Client, npc: Npc): Boolean {
    LegendsGuildGateService.openForGuardTalk(client, npc)
    return true
}
