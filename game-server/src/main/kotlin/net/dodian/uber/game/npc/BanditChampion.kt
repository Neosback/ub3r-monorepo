package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object BanditChampion : NpcScript("BanditChampion", 738) {
    override val definition = define {
        stats {
            attack = 59
            attackAnimation = 451
            deathAnimation = 2304
            defence = 50
            examine = "A very tough-looking bandit."
            hitpoints = 50
            magic = 0
            ranged = 0
            respawnTicks = 155
            strength = 80
        }

        spawns(
            spawn(3365, 2992, profile = profile("bandit_champion.pollnivneach")),
        )
    }
}
