package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object GiantRockCrab : NpcScript("GiantRockCrab", 2261) {
    override val definition = define {
        stats {
            attack = 50
            attackAnimation = 1312
            deathAnimation = 1314
            defence = 200
            examine = "No one likes crabs... especially really big ones!"
            hitpoints = 180
            magic = 1
            ranged = 1
            respawnTicks = 180
            strength = 80
        }

        spawns(
            spawn(2778, 3208, profile = profile("giant_rock_crab.brimhaven")),
        )
    }
}
