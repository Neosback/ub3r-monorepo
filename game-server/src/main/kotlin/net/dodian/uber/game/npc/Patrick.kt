package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Patrick : NpcScript("Patrick", 910) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 2304
            defence = 0
            hitpoints = 400
            magic = 0
            ranged = 0
            respawnTicks = 300
            strength = 0
        }

        spawns(
            spawn(2906, 9680, profile = profile("patrick.black_knights_base")),
        )
    }
}
