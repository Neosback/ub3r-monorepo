package net.dodian.uber.game.npc

internal object FishingSpot3 : NpcFamily by npcFamily("Fishing spot", 1511, block = {
    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2599, 3419)
        spawn(2602, 3414)
        spawn(2838, 3431)
        spawn(2855, 3423)
    }
})
