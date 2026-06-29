package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Molanisk : NpcScript("Molanisk", 1) {
    override val definition = define {
        stats {
            attack = 40
            attackAnimation = 806
            deathAnimation = 2304
            defence = 50
            examine = "A strange mole-like being."
            hitpoints = 52
            magic = 0
            ranged = 1
            respawnTicks = 1
            strength = 40
        }

        spawns(
            spawn(2574, 9625, profile = profile("molanisk.ogres")),
        )
    }
}
