package net.dodian.uber.game.npc

internal object Vampyre : NpcFamily by npcFamily("Vampyre", 3137, block = {
    cache {
        examine = "It looks really hungry."
    }

    server {
        attackAnimation = 1264
        attack = 60
        strength = 78
        defence = 45
        hitpoints = 85
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3544, 3499)
        spawn(3545, 3503)
        spawn(3546, 3496)
        spawn(3548, 3500)
        spawn(3549, 3505)
        spawn(3550, 3495)
        spawn(3552, 3501)
        spawn(3553, 3498)
        spawn(3553, 3506)
        spawn(3556, 3500)
        spawn(3556, 3503)
    }
})
