package net.dodian.uber.game.npc

internal object BlackKnightTitan : NpcFamily by npcFamily("Black Knight Titan", 4067, block = {
    cache {
        examine = "Mad because bad"
    }

    server {
        attackAnimation = 128
        deathAnimation = 131
        respawnTicks = 180
        attack = 110
        strength = 160
        defence = 150
        hitpoints = 335
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2564, 9507)
    }
})
