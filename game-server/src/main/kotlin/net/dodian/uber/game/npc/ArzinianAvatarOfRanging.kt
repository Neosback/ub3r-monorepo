package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object ArzinianAvatarOfRanging : NpcScript("ArzinianAvatarOfRanging", 1230) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            examine = "It is the avatar of the Arzinian Being of Bordanzan, representing ranging."
            hitpoints = 13
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        spawns(
            spawn(2985, 9638, profile = profile("arzinian_avatar_of_ranging.rat_pits")),
            spawn(2987, 9640, profile = profile("arzinian_avatar_of_ranging.rat_pits")),
            spawn(2988, 9637, profile = profile("arzinian_avatar_of_ranging.rat_pits")),
            spawn(2989, 9634, profile = profile("arzinian_avatar_of_ranging.rat_pits")),
        )
    }
}
