package net.dodian.uber.game.npc

internal object Koftik : NpcFamily by npcFamily("Koftik", 972, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 70
        attack = 68
        strength = 70
        defence = 50
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2912, 4390)
    }
})
