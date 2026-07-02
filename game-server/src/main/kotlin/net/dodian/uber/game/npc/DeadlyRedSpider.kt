package net.dodian.uber.game.npc

internal object DeadlyRedSpider : NpcFamily by npcFamily("Deadly red spider", 63, block = {
    server {
        deathAnimation = 2304
        hitpoints = 60
        attack = 19
        strength = 21
        defence = 16
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3210, 9897)
        spawn(3213, 9890)
    }
})
