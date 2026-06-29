package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object ArzinianAvatarOfStrength : NpcScript("ArzinianAvatarOfStrength", 1227, 1228, 1229) {
    override val definition = define {
        stats {
            attack = 70
            attackAnimation = 806
            deathAnimation = 2304
            defence = 50
            examine = "It is the avatar of the Arzinian Being of Bordanzan, representing strength."
            hitpoints = 70
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 65
        }

        spawns(
            spawn(2960, 9632, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2962, 9633, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2963, 9632, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2963, 9635, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2964, 9636, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2962, 9633, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2962, 9635, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2984, 9639, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2985, 9632, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2985, 9633, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2985, 9635, profile = profile("arzinian_avatar_of_strength.rat_pits")),
            spawn(2988, 9640, profile = profile("arzinian_avatar_of_strength.rat_pits")),
        )
    }
}
