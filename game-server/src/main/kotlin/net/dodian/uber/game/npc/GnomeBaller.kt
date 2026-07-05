package net.dodian.uber.game.npc

internal object GnomeBaller : NpcFamily by npcFamily("Gnome baller", 626, block = {
    cache {
        examine = "A bunch of legs, eyes and teeth."
    }

    server {
        defenceAnimation = 5320
        deathAnimation = 2304
        hitpoints = 40
        attack = 60
        strength = 30
        defence = 40
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3215, 9935)
        spawn(3216, 9931)
        spawn(3216, 9939)
        spawn(3220, 9929)
        spawn(3222, 9939)
        spawn(3223, 9928)
        spawn(3226, 9932)
        spawn(3226, 9936)
    }
})
