package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object CabinBoyJenkins : NpcScript("CabinBoyJenkins", 826) {
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
            spawn(2526, 4778, profile = profile("cabin_boy_jenkins.mage_arena_bank")),
        )
    }
}
