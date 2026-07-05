package net.dodian.uber.game.npc

internal object MountedTerrorbirdGnome : NpcFamily by npcFamily("Mounted terrorbird gnome", 5971, block = {
    cache {
        examine = "These gnomes know how to get around!"
    }

    server {
        defenceAnimation = 6794
        hitpoints = 55
        attack = 40
        strength = 40
        defence = 40
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2441, 9864)
        spawn(2444, 9872)
        spawn(2451, 9860)
        spawn(2461, 9861)
        spawn(2472, 9865)
        spawn(2480, 9863)
        spawn(2481, 9870)
        spawn(2489, 9863)
    }
})
