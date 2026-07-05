package net.dodian.uber.game.npc

internal object RockCrab : NpcFamily by npcFamily("Rock crab", 100, block = {
    cache {
        examine = "No one likes crabs... Disguised as rocks:  A rocky outcrop."
    }

    server {
        attackAnimation = 1312
        deathAnimation = 1314
        attack = 1
        strength = 1
        defence = 1
        hitpoints = 40
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(2780, 3216)
        spawn(2780, 3219)
        spawn(2781, 3213)
        spawn(2783, 3218)
        spawn(2784, 3212)
        spawn(2785, 3210)
        spawn(2785, 3215)
        spawn(2788, 3209)
        spawn(2789, 3213)
        spawn(2791, 3210)
        spawn(2793, 3213)
        spawn(2794, 3210)
        spawn(2796, 3212)
        spawn(2798, 3209)
    }
})
