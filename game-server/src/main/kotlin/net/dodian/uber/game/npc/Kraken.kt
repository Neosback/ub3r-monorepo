package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Kraken : NpcScript("Kraken", 6640) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 836
            defence = 1
            examine = "Release the Kraken..eh baby?!"
            hitpoints = 255
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 1
        }

        spawns(
            spawn(2598, 3421, profile = profile("kraken.fishing_guild")),
        )
    }
}
