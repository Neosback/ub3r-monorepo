package net.dodian.uber.game.npc

internal object Diango : NpcFamily by npcFamily("Diango", 970, block = {
    server {
        deathAnimation = 2304
        hitpoints = 70
        attack = 68
        strength = 70
        defence = 50
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2913, 4388)
    }
})
