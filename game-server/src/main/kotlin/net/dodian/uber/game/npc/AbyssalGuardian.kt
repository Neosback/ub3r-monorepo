package net.dodian.uber.game.npc

internal object AbyssalGuardian : NpcFamily by npcFamily("Abyssal guardian", 2585, block = {
    runtime {
        attackAnimation = 2187
        deathAnimation = 2189
        respawnTicks = 90
        attack = 80
        strength = 65
        defence = 130
        hitpoints = 120
        magic = 2650
        ranged = 1
    }

    spawns {
        spawn(2627, 3082)
    }
})
