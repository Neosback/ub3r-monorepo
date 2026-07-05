package net.dodian.uber.game.npc

internal object Koftik4 : NpcFamily by npcFamily("Koftik", 975, block = {
    server {
        defenceAnimation = 1340
        deathAnimation = 2304
        hitpoints = 120
        attack = 68
        strength = 70
        defence = 71
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2917, 4384)
    }
})
