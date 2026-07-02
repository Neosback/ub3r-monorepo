package net.dodian.uber.game.npc

internal object AberrantSpectre : NpcFamily by npcFamily("Aberrant spectre", 2, block = {
    server {
        attackAnimation = 1507
        deathAnimation = 1508
        respawnTicks = 35
        attack = 80
        strength = 80
        defence = 80
        hitpoints = 120
        magic = 105
        ranged = 1
    }

    spawns {
        spawn(2640, 9864)
        spawn(2642, 9862)
        spawn(2643, 9866)
        spawn(2644, 9869)
        spawn(2645, 9861)
        spawn(2646, 9864)
        spawn(2648, 9861)
        spawn(2648, 9865)
    }
})
