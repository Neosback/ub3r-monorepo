package net.dodian.uber.game.npc

internal object Watchman : NpcFamily by npcFamily("Watchman", 5420, block = {
    cache {
        name = "Watchman"
    }

    server {
        deathAnimation = 2304
        respawnTicks = 30
        attack = 16
        strength = 30
        defence = 20
        hitpoints = 22
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2583, 3102, face = SOUTH_EAST)
        spawn(2583, 3105, face = SOUTH)
        spawn(2584, 3100, face = SOUTH)
        spawn(2585, 3104, face = EAST)
        spawn(2585, 3107, face = SOUTH_WEST)
        spawn(2586, 3102, face = WEST)
        spawn(2588, 3104, face = WEST)
        spawn(2588, 3106)
        spawn(3242, 9894)
        spawn(3244, 9894)
    }
})
