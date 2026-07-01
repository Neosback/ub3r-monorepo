package net.dodian.uber.game.npc

internal object MossGiant : NpcFamily by npcFamily("Moss giant", 3851, block = {
    runtime {
        attackAnimation = 4652
        deathAnimation = 4673
        respawnTicks = 35
        attack = 30
        strength = 40
        defence = 30
        hitpoints = 47
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2572, 3088)
        spawn(2573, 3082)
        spawn(2574, 3079)
        spawn(2575, 3086)
        spawn(2576, 3076)
        spawn(2576, 3089)
        spawn(2578, 3079)
        spawn(2579, 3083)
        spawn(2579, 3089)
        spawn(3155, 9903)
        spawn(3164, 9887)
        spawn(3165, 9879)
        spawn(3166, 9896)
        spawn(3169, 9890)
        spawn(3170, 9884)
        spawn(3174, 9896)
        spawn(3180, 9886)
        spawn(3184, 9892)
    }
})
