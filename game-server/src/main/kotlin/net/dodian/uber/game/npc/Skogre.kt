package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Skogre : NpcScript("Skogre", 872) {
    override val definition = define {
        stats {
            attack = 20
            attackAnimation = 806
            deathAnimation = 2304
            defence = 35
            examine = "It's falling apart!"
            hitpoints = 71
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 36
        }

        spawns(
            spawn(2390, 9895, profile = profile("skogre.brimstail")),
            spawn(2393, 9899, profile = profile("skogre.brimstail")),
            spawn(2396, 9891, profile = profile("skogre.brimstail")),
        )
    }
}
