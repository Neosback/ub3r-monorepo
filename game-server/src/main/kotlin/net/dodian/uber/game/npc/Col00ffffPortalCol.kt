package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Col00ffffPortalCol : NpcScript("Col00ffffPortalCol", 3086) {

    override val definition = define {
        stats {
            hitpoints = 0
            attack = 0
            defence = 0
            strength = 0
            magic = 0
            ranged = 0
            attackAnimation = 806
            deathAnimation = 2304
            respawnTicks = 60
        }

        spawns(
            spawn(2590, 3102, face = SOUTH, profile = profile("yanille"))
        )
    }
}
