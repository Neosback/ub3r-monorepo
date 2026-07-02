package net.dodian.uber.game.npc

internal object Ungadulu : NpcFamily by npcFamily("Ungadulu", 3957, block = {
    cache {
        examine = "Man of herblore"
    }

    server {
        attackAnimation = 406
        deathAnimation = 2304
        respawnTicks = 180
        attack = 110
        strength = 80
        defence = 90
        hitpoints = 120
        ranged = 180
        magic = 65
    }

    spawns {
        spawn(2889, 3424)
    }
})
