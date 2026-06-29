package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Mourner : NpcScript("Mourner", 9017) {
    override val definition = define {
        stats {
            attack = 8
            attackAnimation = 2080
            deathAnimation = 2304
            defence = 8
            examine = "A Mourner, or plague healer."
            hitpoints = 19
            magic = 1
            ranged = 1
            respawnTicks = 90
            strength = 8
        }

        spawns(
            spawn(2552, 3278, profile = profile("mourner.ardougne")),
        )
    }
}
