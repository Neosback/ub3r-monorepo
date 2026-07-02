package net.dodian.uber.game.npc

internal object Zombie2 : NpcFamily by npcFamily("Zombie", 75, block = {
    server {
        deathAnimation = 2304
        hitpoints = 30
        attack = 17
        strength = 17
        defence = 17
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3249, 9900)
        spawn(3250, 9906)
        spawn(3251, 9897)
        spawn(3252, 9903)
        spawn(3255, 9893)
        spawn(3256, 9907)
        spawn(3266, 9893)
    }
})
