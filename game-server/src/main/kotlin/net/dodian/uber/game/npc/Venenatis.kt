package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Venenatis : NpcScript("Venenatis", 6610) {
    override val definition = define {
        stats {
            attack = 300
            attackAnimation = 5319
            deathAnimation = 5321
            defence = 321
            examine = "That'll get your arachnophobia going..."
            hitpoints = 850
            magic = 300
            ranged = 350
            respawnTicks = 250
            strength = 200
        }

        spawns(
            spawn(3220, 9933, profile = profile("venenatis.here_be_dead_stuff")),
        )
    }
}
