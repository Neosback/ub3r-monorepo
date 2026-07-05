package net.dodian.uber.game.npc

internal object Ghoul : NpcFamily by npcFamily("Ghoul", 289, block = {
    cache {
        examine = "It's totally savage."
    }

    server {
        attackAnimation = 422
        deathAnimation = 2304
        respawnTicks = 40
        attack = 90
        strength = 100
        defence = 80
        hitpoints = 115
        ranged = 30
        magic = 1
    }

    spawns {
        spawn(2567, 9526)
        spawn(2569, 9528)
        spawn(2570, 9531)
        spawn(2572, 9524)
        spawn(2572, 9526)
        spawn(2575, 9524)
        spawn(2575, 9527)
        spawn(2575, 9529)
        spawn(2578, 9523)
    }
})
