package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Stonemason : NpcScript("Stonemason", 3098) {
    override val definition = define {
        stats {
            attack = 30
            attackAnimation = 806
            deathAnimation = 2304
            defence = 15
            hitpoints = 30
            magic = 0
            ranged = 0
            respawnTicks = 20
            strength = 15
        }

        spawns(
            spawn(2884, 3430, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2885, 3422, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2885, 3435, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2887, 3429, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2888, 3440, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2891, 3422, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2893, 3433, profile = profile("stonemason.taverley_burthorpe")),
            spawn(2897, 3442, profile = profile("stonemason.taverley_burthorpe")),
        )
    }
}
