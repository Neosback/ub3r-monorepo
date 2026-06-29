package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object FishingSpot : NpcScript("FishingSpot", 1510, 1511, 1514, 1517) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 0
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        spawns(
            spawn(2836, 3431, profile = profile("fishing_spot.catherby")),
            spawn(2845, 3429, profile = profile("fishing_spot.catherby")),
            spawn(2838, 3431, profile = profile("fishing_spot.catherby")),
            spawn(2855, 3423, profile = profile("fishing_spot.catherby")),
            spawn(2859, 3426, profile = profile("fishing_spot.catherby")),
            spawn(2844, 3429, profile = profile("fishing_spot.catherby")),
            spawn(2598, 3422, profile = profile("fishing_spot.fishing_guild")),
            spawn(2602, 3419, profile = profile("fishing_spot.fishing_guild")),
            spawn(2599, 3419, profile = profile("fishing_spot.fishing_guild")),
            spawn(2602, 3414, profile = profile("fishing_spot.fishing_guild")),
            spawn(2601, 3422, profile = profile("fishing_spot.fishing_guild")),
            spawn(2604, 3417, profile = profile("fishing_spot.fishing_guild")),
            spawn(2605, 3421, profile = profile("fishing_spot.fishing_guild")),
            spawn(2598, 3425, profile = profile("fishing_spot.fishing_guild")),
            spawn(2603, 3426, profile = profile("fishing_spot.fishing_guild")),
        )
    }
}
