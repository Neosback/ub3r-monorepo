package net.dodian.uber.game.npc

internal object FishingSpot5 : NpcFamily by npcFamily("Fishing spot", 1517, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2598, 3425)
    }
})
