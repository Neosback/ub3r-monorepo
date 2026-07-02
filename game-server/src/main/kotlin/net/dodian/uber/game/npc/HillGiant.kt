package net.dodian.uber.game.npc

internal object HillGiant : NpcFamily by npcFamily("Hill giant", 2098, block = {
    server {
        attackAnimation = 4652
        deathAnimation = 4653
        respawnTicks = 30
        attack = 20
        strength = 20
        defence = 20
        hitpoints = 35
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2879, 3470)
        spawn(2882, 3461)
        spawn(2883, 3466)
        spawn(2883, 3473)
        spawn(2885, 3469)
        spawn(2886, 3463)
        spawn(2886, 3474)
    }
})
