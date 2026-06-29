package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object CombatStone : NpcScript("CombatStone", 2745) {
    override val definition = define {
        stats {
            attack = 0
            attackAnimation = 806
            deathAnimation = 836
            defence = 0
            hitpoints = 0
            magic = 0
            ranged = 0
            respawnTicks = 60
            strength = 0
        }

        spawns(
            spawn(2480, 3428, z = 2, profile = profile("combat_stone.tree_gnome_stronghold")),
        )
    }
}
