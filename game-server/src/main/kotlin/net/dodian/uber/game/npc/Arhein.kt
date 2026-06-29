package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Arhein : NpcScript("Arhein", 3200) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 100
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        spawns(
            spawn(2805, 3427, profile = profile("arhein.catherby")),
        )
    }
}
