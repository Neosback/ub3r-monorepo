package net.dodian.uber.game.npc

internal object BillyGoat : NpcFamily by npcFamily("Billy goat", 1794, block = {
    cache {
        examine = "Billy was his name"
    }

    server {
        defenceAnimation = 251
        attackAnimation = 4936
        deathAnimation = 253
        respawnTicks = 150
        attack = 40
        strength = 40
        defence = 40
        hitpoints = 42
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3160, 3043)
    }
})
