package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.utilities.Utils

internal object UnknownNpc5792 : NpcFamily by npcFamily("Party Pete", 5792, block = {
    definition {
        examine = "He likes to party."
        name = "Party Pete"
    }

    options {
        talkTo(handler = ::handlePartyPeteTeleport)
    }

    spawns {
        spawn(3090, 3492)
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handlePartyPeteTeleport(client: Client, npc: Npc): Boolean {
    client.triggerTele(3045 + Utils.random(2), 3375 + Utils.random(2), 0, false)
    client.sendMessage("Party Pete teleports you to the party room!")
    return true
}
