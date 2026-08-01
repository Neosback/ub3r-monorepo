package net.dodian.uber.game.npc

internal object Ghost : NpcFamily by npcFamily("Ghost", 92, block = {
    definition {
        examine = "Eeek! A ghost! Grave of Scorpius:  Spooky."
    }

    server {
        defenceAnimation = 5533
        attackAnimation = 5532
        deathAnimation = 5534
        respawnTicks = 30
        attack = 10
        strength = 20
        defence = 15
        hitpoints = 20
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2584, 9489)
        spawn(2586, 9487)
        spawn(2589, 9489)
        spawn(2590, 9487)
        spawn(2591, 9486)
        spawn(2593, 9489)
        spawn(3202, 9881)
        spawn(3205, 9907)
        spawn(3206, 9883)
        spawn(3208, 9904)
        spawn(3211, 9907)
        spawn(3214, 9905)
        spawn(3218, 9907)
        spawn(3222, 9906)
        spawn(3225, 9909)
        spawn(3226, 9903)
        spawn(3230, 9906)
        spawn(3238, 9867)
        spawn(3277, 9892)
        spawn(3282, 9897)
        spawn(3284, 9891)
    }
})
