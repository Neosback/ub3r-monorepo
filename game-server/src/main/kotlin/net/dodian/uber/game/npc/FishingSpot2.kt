package net.dodian.uber.game.npc

internal object FishingSpot2 : NpcFamily by npcFamily("Fishing spot", 1510, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 2000
    }

    spawns {
        spawn(2598, 3422)
        spawn(2602, 3419)
        spawn(2836, 3431)
    }
})
