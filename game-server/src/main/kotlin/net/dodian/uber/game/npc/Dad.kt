package net.dodian.uber.game.npc

internal object Dad : NpcFamily by npcFamily("Dad", 4130, block = {
    runtime {
        attackAnimation = 284
        deathAnimation = 287
        respawnTicks = 180
        attack = 80
        strength = 110
        defence = 90
        hitpoints = 120
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(2541, 3092, face = SOUTH_EAST)
    }
})
