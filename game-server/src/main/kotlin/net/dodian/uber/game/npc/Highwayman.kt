package net.dodian.uber.game.npc

internal object Highwayman : NpcFamily by npcFamily("Highwayman", 518, block = {
    cache {
        examine = "Likes people spending money."
        name = "Highwayman"
    }

    server {
        deathAnimation = 2304
        respawnTicks = 30
        attack = 6
        strength = 12
        defence = 2
        hitpoints = 11
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2595, 3102, face = SOUTH)
    }
})
