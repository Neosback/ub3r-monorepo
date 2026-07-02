package net.dodian.uber.game.npc

internal object TheShaikahan : NpcFamily by npcFamily("The Shaikahan", 1173, block = {
    cache {
        name = "The Shaikahan"
    }

    server {
        deathAnimation = 2304
        hitpoints = 100
        attack = 1
        strength = 1
        defence = 1
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2745, 3475)
        spawn(2746, 3472)
        spawn(2747, 3474)
        spawn(2748, 3471)
        spawn(2748, 3476)
        spawn(2749, 3473)
        spawn(2750, 3475)
    }
})
