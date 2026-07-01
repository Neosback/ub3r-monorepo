package net.dodian.uber.game.npc

internal object CrawlingHand : NpcFamily by npcFamily("Crawling Hand", 449, block = {
    runtime {
        attackAnimation = 1592
        deathAnimation = 1590
        respawnTicks = 30
        attack = 15
        strength = 8
        defence = 8
        hitpoints = 16
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2527, 3297)
        spawn(2529, 3287)
        spawn(2529, 3294)
        spawn(2529, 3300)
        spawn(2530, 3290)
        spawn(2531, 3298)
        spawn(2534, 3297)
        spawn(2538, 3295)
        spawn(2540, 3287)
        spawn(2540, 3298)
        spawn(2541, 3291)
        spawn(2542, 3295)
        spawn(2544, 3286)
        spawn(2544, 3290)
        spawn(2546, 3305)
        spawn(2547, 3294)
        spawn(2548, 3303)
        spawn(2548, 3308)
        spawn(2550, 3303)
        spawn(2551, 3305)
        spawn(2552, 3303)
        spawn(2561, 3300)
    }
})
