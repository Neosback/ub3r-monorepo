package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object CorporealBeast : NpcScript("CorporealBeast", 319) {
    override val definition = define {
        stats {
            attack = 320
            attackAnimation = 1682
            deathAnimation = 1676
            defence = 310
            examine = "A vision of supernatural horror."
            hitpoints = 2000
            magic = 350
            ranged = 150
            respawnTicks = 60
            strength = 320
        }

        spawns(
            spawn(3245, 2789, profile = profile("corporeal_beast.menaphos")),
        )
    }
}
