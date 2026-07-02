package net.dodian.uber.game.npc

internal object RedDragon : NpcFamily by npcFamily("Red dragon", 247, block = {
    cache {
        examine = "Red and fierce!"
    }

    server {
        attackAnimation = 91
        deathAnimation = 92
        respawnTicks = 40
        attack = 130
        strength = 140
        defence = 130
        hitpoints = 150
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3219, 9379)
        spawn(3221, 9375)
        spawn(3222, 9384)
        spawn(3223, 9393)
        spawn(3223, 9398)
        spawn(3228, 9392)
        spawn(3231, 9397)
        spawn(3243, 9357)
        spawn(3243, 9367)
    }
})
