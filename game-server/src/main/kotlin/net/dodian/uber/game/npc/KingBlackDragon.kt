package net.dodian.uber.game.npc

internal object KingBlackDragon : NpcFamily by npcFamily("King black dragon", 239, block = {
    cache {
        examine = "Rawr xD UwU"
    }

    server {
        defenceAnimation = 90
        attackAnimation = 81
        deathAnimation = 92
        respawnTicks = 180
        attack = 210
        strength = 250
        defence = 200
        hitpoints = 330
        ranged = 320
        magic = 1200
    }

    spawns {
        spawn(3315, 9376)
    }
})
