package net.dodian.uber.game.npc

internal object Banshee : NpcFamily by npcFamily("Banshee", 414, block = {
    server {
        attackAnimation = 1523
        deathAnimation = 1524
        attack = 40
        strength = 60
        defence = 20
        hitpoints = 60
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3424, 3462)
        spawn(3426, 3465)
        spawn(3427, 3458)
        spawn(3428, 3460)
        spawn(3429, 3464)
        spawn(3430, 3458)
        spawn(3431, 3467)
        spawn(3432, 3461)
        spawn(3434, 3465)
        spawn(3435, 3461)
        spawn(3437, 3457)
        spawn(3437, 3467)
        spawn(3439, 3459)
        spawn(3441, 3462)
        spawn(3441, 3466)
    }
})
