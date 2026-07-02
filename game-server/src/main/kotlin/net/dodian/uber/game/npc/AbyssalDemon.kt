package net.dodian.uber.game.npc

internal object AbyssalDemon : NpcFamily by npcFamily("Abyssal demon", 415, block = {
    cache {
        examine = "Wonder where their whip is from?"
    }

    server {
        attackAnimation = 1537
        deathAnimation = 1538
        attack = 110
        strength = 80
        defence = 110
        hitpoints = 200
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2864, 9781)
        spawn(2865, 9778)
        spawn(2866, 9775)
        spawn(2867, 9783)
        spawn(2868, 9772)
        spawn(2868, 9779)
        spawn(2869, 9777)
        spawn(2870, 9775)
        spawn(2870, 9780)
        spawn(2870, 9784)
        spawn(2872, 9786)
        spawn(2873, 9777)
        spawn(2873, 9782)
        spawn(2875, 9774)
        spawn(2875, 9779)
        spawn(2875, 9784)
        spawn(2875, 9787)
        spawn(2876, 9769)
        spawn(2877, 9772)
        spawn(2880, 9769)
    }
})
