package net.dodian.uber.game.npc

internal object IceQueen : NpcFamily by npcFamily("Ice Queen", 4922, block = {
    cache {
        examine = "Stone cold queen."
    }

    runtime {
        deathAnimation = 2304
        respawnTicks = 180
        attack = 95
        strength = 135
        defence = 115
        hitpoints = 285
        magic = 1200
        ranged = 1
    }

    spawns {
        spawn(2865, 9953)
    }
})
