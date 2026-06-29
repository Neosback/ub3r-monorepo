package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Seagull : NpcScript("Seagull", 1337, 1338) {
    override val definition = define {
        stats {
            attack = 1
            attackAnimation = 806
            deathAnimation = 2304
            defence = 1
            examine = "A messy bird."
            hitpoints = 6
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 1
        }

        spawns(
            spawn(1811, 4495, profile = profile("seagull.sub_level_5")),
            spawn(1842, 4494, profile = profile("seagull.sub_level_5")),
        )
    }
}
