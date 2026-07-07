package net.dodian.uber.game.npc

internal object HeadMourners : NpcFamily by npcFamily("Head Mourners", 9017, block = {
    definition {
        name = "Head Mourners"
    }

    server {
        attackAnimation = 2080
        deathAnimation = 2304
        respawnTicks = 90
        attack = 132
        strength = 230
        defence = 122
        hitpoints = 280
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2552, 3278)
    }
})
