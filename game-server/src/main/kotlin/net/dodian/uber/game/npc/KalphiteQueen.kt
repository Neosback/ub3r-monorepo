package net.dodian.uber.game.npc

internal object KalphiteQueen : NpcFamily by npcFamily("Kalphite queen", 4303, block = {
    cache {
        examine = "Queen of all kalphites."
    }

    server {
        attackAnimation = 6241
        deathAnimation = 6242
        respawnTicks = 250
        attack = 180
        strength = 290
        defence = 250
        hitpoints = 900
        ranged = 240
        magic = 150
    }

    spawns {
        spawn(3483, 9492)
    }
})
