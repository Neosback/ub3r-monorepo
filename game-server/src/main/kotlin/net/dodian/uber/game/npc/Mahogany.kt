package net.dodian.uber.game.npc

internal object Mahogany : NpcFamily by npcFamily("Mahogany", 2534, block = {
    server {
        deathAnimation = 2304
        hitpoints = 75
        attack = 63
        strength = 63
        defence = 68
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2677, 3585)
        spawn(2682, 3596)
    }
})
