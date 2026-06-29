package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Chancy : NpcScript("Chancy", 1105) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 96
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        spawns(
            spawn(3589, 3477, profile = profile("chancy.morytania")),
        )
    }
}
