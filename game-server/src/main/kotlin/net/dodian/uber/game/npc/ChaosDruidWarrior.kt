package net.dodian.uber.game.npc

internal object ChaosDruidWarrior : NpcFamily by npcFamily("Chaos druid warrior", 532, block = {
    definition {
        examine = "Sells superior staffs."
        name = "Chaos druid warrior"
    }

    server {
        attackAnimation = 401
        deathAnimation = 2304
        respawnTicks = 30
        attack = 18
        strength = 32
        defence = 22
        hitpoints = 24
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2577, 3103, face = EAST)
        spawn(2578, 3106, face = SOUTH_EAST)
        spawn(2579, 3101, face = SOUTH)
        spawn(2579, 3104, face = WEST)
        spawn(2581, 3100, face = SOUTH)
        spawn(2581, 3103, face = SOUTH_EAST)
        spawn(2581, 3106, face = SOUTH_WEST)
    }
})
