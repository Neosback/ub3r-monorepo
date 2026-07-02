package net.dodian.uber.game.npc

internal object BanditBoss : NpcFamily by npcFamily("Bandit boss", 690, block = {
    cache {
        examine = "Hates religion"
    }

    server {
        attackAnimation = 386
        deathAnimation = 2304
        respawnTicks = 155
        attack = 82
        strength = 110
        defence = 85
        hitpoints = 138
        ranged = 69
        magic = 69
    }

    spawns {
        spawn(3177, 2987)
    }
})
