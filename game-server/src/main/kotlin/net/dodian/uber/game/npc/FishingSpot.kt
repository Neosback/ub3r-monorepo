package net.dodian.uber.game.npc

internal object FishingSpot : NpcFamily by npcFamily("Fishing spot", 319, block = {
    server {
        deathAnimation = 2304
        hitpoints = 2000
        attack = 320
        strength = 320
        defence = 310
        magic = 350
        ranged = 150
    }

    spawns {
        spawn(3245, 2789)
    }
})
