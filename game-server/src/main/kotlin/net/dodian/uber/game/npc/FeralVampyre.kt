package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object FeralVampyre : NpcScript("FeralVampyre", 3137) {
    override val definition = define {
        stats {
            attack = 65
            attackAnimation = 1264
            deathAnimation = 836
            defence = 81
            examine = "A feral vampyre. It looks really hungry!"
            hitpoints = 60
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 66
        }

        spawns(
            spawn(3544, 3499, profile = profile("feral_vampyre.morytania")),
            spawn(3545, 3503, profile = profile("feral_vampyre.morytania")),
            spawn(3546, 3496, profile = profile("feral_vampyre.morytania")),
            spawn(3548, 3500, profile = profile("feral_vampyre.morytania")),
            spawn(3549, 3505, profile = profile("feral_vampyre.morytania")),
            spawn(3550, 3495, profile = profile("feral_vampyre.morytania")),
            spawn(3552, 3501, profile = profile("feral_vampyre.morytania")),
            spawn(3553, 3498, profile = profile("feral_vampyre.morytania")),
            spawn(3553, 3506, profile = profile("feral_vampyre.morytania")),
            spawn(3556, 3500, profile = profile("feral_vampyre.morytania")),
            spawn(3556, 3503, profile = profile("feral_vampyre.morytania")),
        )
    }
}
