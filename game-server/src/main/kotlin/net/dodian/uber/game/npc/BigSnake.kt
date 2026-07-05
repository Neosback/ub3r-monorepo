package net.dodian.uber.game.npc

internal object BigSnake : NpcFamily by npcFamily("Big Snake", 2978, block = {
    cache {
        examine = "A big snake."
    }

    server {
        hitpoints = 120
        attack = 60
        strength = 60
        defence = 60
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2392, 9873)
    }
})
