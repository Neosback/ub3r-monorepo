package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Spinolyp : NpcScript("Spinolyp", 5961) {
    override val definition = define {
        stats {
            attack = 10
            attackAnimation = 806
            deathAnimation = 836
            defence = 10
            examine = "A sneaky, spiny, subterranean sea-dwelling scamp."
            hitpoints = 100
            magic = 1
            ranged = 100
            respawnTicks = 60
            strength = 10
        }

        spawns(
            spawn(2908, 4388, profile = profile("spinolyp.dagannoth_kings")),
        )
    }
}
