package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Woman : NpcScript("Woman", 6990) {
    override val definition = define {
        stats {
            attack = 1
            attackAnimation = 806
            deathAnimation = 836
            defence = 1
            examine = "One of Gielinor's many citizens."
            hitpoints = 7
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 1
        }

        spawns(
            spawn(3091, 3496, face = SOUTH, profile = profile("woman.edgeville")),
        )
    }
}
