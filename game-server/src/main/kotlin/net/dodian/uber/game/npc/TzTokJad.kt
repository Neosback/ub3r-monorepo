package net.dodian.uber.game.npc

internal object TzTokJad : NpcFamily by npcFamily("TzTok-Jad", 3127, block = {
    cache {
        examine = "Scariest boss in Dodian"
    }

    server {
        defenceAnimation = 2653
        attackAnimation = 2655
        deathAnimation = 2654
        respawnTicks = 360
        attack = 480
        strength = 550
        defence = 420
        hitpoints = 2400
        ranged = 890
        magic = 620
    }

    spawns {
        spawn(2396, 5084, face = SOUTH_WEST)
    }
})
