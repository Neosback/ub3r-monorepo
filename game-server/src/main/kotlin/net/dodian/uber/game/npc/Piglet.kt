package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Piglet : NpcScript("Piglet", 2810) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 0
            deathAnimation = 0
            defence = 0
            hitpoints = 0
            magic = 0
            ranged = 0
            respawnTicks = 0
            strength = 0
        }

        spawns(
            spawn(2605, 3399, profile = profile("piglet.fishing_guild")),
        )
    }
}
