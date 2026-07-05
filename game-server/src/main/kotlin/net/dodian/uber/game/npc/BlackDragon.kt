package net.dodian.uber.game.npc

internal object BlackDragon : NpcFamily by npcFamily("Black dragon", 252, block = {
    cache {
        examine = "A fierce dragon with black scales!"
    }

    server {
        attackAnimation = 91
        deathAnimation = 92
        respawnTicks = 40
        attack = 190
        strength = 210
        defence = 190
        hitpoints = 200
        magic = 100
        ranged = 1
    }

    spawns {
        spawn(3208, 9349)
        spawn(3211, 9351)
        spawn(3211, 9360)
        spawn(3213, 9362)
        spawn(3214, 9355)
        spawn(3216, 9349)
        spawn(3217, 9364)
    }
})
