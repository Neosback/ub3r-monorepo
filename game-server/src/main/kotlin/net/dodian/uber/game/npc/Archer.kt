package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object Archer : NpcScript("Archer", 1157) {
    override val definition = define {
        stats {
            attack = 80
            attackAnimation = 426
            deathAnimation = 836
            defence = 140
            hitpoints = 200
            magic = 130
            ranged = 135
            respawnTicks = 70
            strength = 90
        }

        spawns(
            spawn(3466, 9483, z = 2, profile = profile("archer.soldiers")),
            spawn(3467, 9489, z = 2, profile = profile("archer.soldiers")),
            spawn(3469, 9479, z = 2, profile = profile("archer.soldiers")),
            spawn(3480, 9476, z = 2, profile = profile("archer.soldiers")),
            spawn(3487, 9476, z = 2, profile = profile("archer.soldiers")),
            spawn(3497, 9475, z = 2, profile = profile("archer.guardians")),
            spawn(3505, 9499, z = 2, profile = profile("archer.guardians")),
            spawn(3506, 9482, z = 2, profile = profile("archer.guardians")),
            spawn(3506, 9493, z = 2, profile = profile("archer.guardians")),
            spawn(3510, 9502, z = 2, profile = profile("archer.guardians")),
            spawn(3511, 9491, z = 2, profile = profile("archer.guardians")),
            spawn(3514, 9495, z = 2, profile = profile("archer.guardians")),
            spawn(3514, 9499, z = 2, profile = profile("archer.guardians")),
        )
    }
}
