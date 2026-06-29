package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object ToughGuy : NpcScript("ToughGuy", 3551) {
    override val definition = define {
        stats {
            attack = 85
            attackAnimation = 401
            deathAnimation = 836
            defence = 50
            examine = "Tough looking Menaphite."
            hitpoints = 75
            magic = 80
            ranged = 0
            respawnTicks = 155
            strength = 50
        }

        spawns(
            spawn(3350, 2949, profile = profile("tough_guy.pollnivneach")),
        )
    }
}
