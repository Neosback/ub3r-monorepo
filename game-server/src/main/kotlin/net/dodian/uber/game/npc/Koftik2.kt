package net.dodian.uber.game.npc

internal object Koftik2 : NpcFamily by npcFamily("Koftik", 973, block = {
    server {
        deathAnimation = 2304
        hitpoints = 120
        attack = 68
        strength = 70
        defence = 71
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2916, 4390)
    }
})
