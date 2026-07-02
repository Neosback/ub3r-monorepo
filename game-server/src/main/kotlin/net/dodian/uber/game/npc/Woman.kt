package net.dodian.uber.game.npc

internal object Woman : NpcFamily by npcFamily("Woman", 6990, block = {
    cache {
        examine = "no examine"
    }

    server {
        hitpoints = 7
        attack = 1
        strength = 1
        defence = 1
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3091, 3496, face = SOUTH)
    }
})
