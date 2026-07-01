package net.dodian.uber.game.npc

internal object Vulture : NpcFamily by npcFamily("Vulture", 1267, block = {
    cache {
        examine = "Love to keep desert clean"
    }

    runtime {
        attackAnimation = 2018
        deathAnimation = 2021
        attack = 25
        strength = 25
        defence = 25
        hitpoints = 30
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(3262, 2969)
        spawn(3264, 2974)
        spawn(3265, 2964)
        spawn(3266, 2969)
        spawn(3268, 2965)
        spawn(3271, 2973)
        spawn(3272, 2969)
        spawn(3273, 2964)
        spawn(3276, 2974)
        spawn(3277, 2967)
        spawn(3279, 2971)
    }
})
