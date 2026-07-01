package net.dodian.uber.game.npc

internal object DeathSpawn : NpcFamily by npcFamily("Death spawn", 10, block = {
    runtime {
        deathAnimation = 2304
        respawnTicks = 35
        attack = 60
        strength = 25
        defence = 30
        hitpoints = 60
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2504, 3305)
        spawn(2508, 3297)
        spawn(2508, 3301)
        spawn(2511, 3315)
        spawn(2512, 3303)
        spawn(2512, 3307)
        spawn(2512, 3311)
        spawn(2513, 3319)
        spawn(2514, 3315)
        spawn(2515, 3309)
        spawn(2516, 3297)
        spawn(2516, 3318)
        spawn(2519, 3319)
        spawn(2519, 3322)
    }
})
