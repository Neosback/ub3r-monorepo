package net.dodian.uber.game.npc

import net.dodian.uber.game.api.content.ContentScheduling
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.netty.listener.out.CameraReset
import net.dodian.uber.game.netty.listener.out.SendCameraShake

internal object Monk : NpcFamily by npcFamily("Monk", 555, block = {
    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleMonkTalkTo)
    }

    spawns {
        spawn(2604, 3092, walkRadius = 3) {
            visibleWhen(::isYanilleMonkVisible)
        }
    }
})

@Suppress("UNUSED_PARAMETER")
private fun handleMonkTalkTo(client: Client, npc: Npc): Boolean {
    client.quests[0]++
    client.sendMessage(
        if (client.playerRights > 1) {
            "Set your quest to: ${client.quests[0]}"
        } else {
            "Suddenly the monk had an urge to dissapear!"
        }
    )
    client.send(SendCameraShake(3, 2, 3, 2))
    ContentScheduling.player(client) {
        delayTicks(6)
        client.send(CameraReset())
    }
    return true
}

private fun isYanilleMonkVisible(client: Client): Boolean = client.quests[0] <= 0
