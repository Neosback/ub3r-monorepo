package net.dodian.uber.game.npc

internal object YtMejKot : NpcFamily by npcFamily("Yt-MejKot", 3123, block = {
    definition {
        examine = "Guarding jad"
    }

    server {
        defenceAnimation = 2635
        attackAnimation = 2637
        deathAnimation = 2638
        attack = 140
        strength = 190
        defence = 130
        hitpoints = 150
        magic = 120
        ranged = 240
    }

    spawns {
        spawn(2386, 5078)
        spawn(2391, 5074)
        spawn(2396, 5078)
        spawn(2400, 5073)
        spawn(2403, 5076)
        spawn(2409, 5074)
        spawn(2409, 5082)
        spawn(2410, 5079)
    }
})
