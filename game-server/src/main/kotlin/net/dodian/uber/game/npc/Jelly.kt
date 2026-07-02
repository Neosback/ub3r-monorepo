package net.dodian.uber.game.npc

internal object Jelly : NpcFamily by npcFamily("Jelly", 437, block = {
    server {
        deathAnimation = 2304
        respawnTicks = 35
        attack = 50
        strength = 50
        defence = 50
        hitpoints = 75
        magic = 45
        ranged = 1
    }

    spawns {
        spawn(2520, 3328)
        spawn(2524, 3325)
        spawn(2527, 3328)
        spawn(2528, 3322)
        spawn(2530, 3326)
        spawn(2531, 3323)
        spawn(2532, 3319)
        spawn(2535, 3322)
        spawn(2535, 3328)
        spawn(2537, 3318)
        spawn(2538, 3321)
        spawn(2538, 3325)
        spawn(2542, 3314)
        spawn(2542, 3318)
        spawn(2548, 3315)
    }
})
