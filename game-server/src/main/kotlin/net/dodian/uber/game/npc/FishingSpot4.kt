package net.dodian.uber.game.npc

internal object FishingSpot4 : NpcFamily by npcFamily("Fishing spot", 1514, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 250
    }

    spawns {
        spawn(2601, 3422)
        spawn(2604, 3417)
        spawn(2605, 3421)
        spawn(2844, 3429)
    }
})
