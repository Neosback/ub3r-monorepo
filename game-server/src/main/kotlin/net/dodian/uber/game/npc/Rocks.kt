package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Rocks : NpcScript("Rocks", 103) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 50
            magic = 0
            ranged = 0
            respawnTicks = 30
            strength = 0
        }

        spawns(
            spawn(3205, 9881, profile = profile("rocks.red_spiders")),
            spawn(3209, 9878, profile = profile("rocks.red_spiders")),
            spawn(3209, 9885, profile = profile("rocks.red_spiders")),
            spawn(3248, 9916, profile = profile("rocks.here_be_dead_stuff")),
            spawn(3251, 9915, profile = profile("rocks.here_be_dead_stuff")),
            spawn(3256, 9916, profile = profile("rocks.here_be_dead_stuff")),
        )
    }
}
