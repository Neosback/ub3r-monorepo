package net.dodian.uber.game.npc

internal object KalphiteGuardian : NpcFamily by npcFamily("Kalphite Guardian", 1157, block = {
    runtime {
        attackAnimation = 6224
        deathAnimation = 6228
        respawnTicks = 70
        attack = 80
        strength = 90
        defence = 80
        hitpoints = 95
    }

    spawns {
        spawn(3466, 9483, z = 2)
        spawn(3467, 9489, z = 2)
        spawn(3469, 9479, z = 2)
        spawn(3480, 9476, z = 2)
        spawn(3487, 9476, z = 2)
        spawn(3497, 9475, z = 2)
        spawn(3505, 9499, z = 2)
        spawn(3506, 9482, z = 2)
        spawn(3506, 9493, z = 2)
        spawn(3510, 9502, z = 2)
        spawn(3511, 9491, z = 2)
        spawn(3514, 9495, z = 2)
        spawn(3514, 9499, z = 2)
    }
})
