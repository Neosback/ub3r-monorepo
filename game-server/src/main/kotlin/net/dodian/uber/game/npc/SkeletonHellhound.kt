package net.dodian.uber.game.npc

internal object SkeletonHellhound : NpcFamily by npcFamily("Skeleton Hellhound", 5054, block = {
    server {
        attackAnimation = 6581
        deathAnimation = 6576
        respawnTicks = 35
        attack = 93
        strength = 81
        defence = 79
        hitpoints = 247
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2900, 9831)
        spawn(2903, 9836)
        spawn(2904, 9831)
        spawn(2905, 9839)
        spawn(2907, 9827)
        spawn(2907, 9843)
        spawn(2908, 9834)
        spawn(2910, 9829)
        spawn(2911, 9837)
        spawn(2911, 9841)
        spawn(2913, 9826)
        spawn(2915, 9832)
        spawn(2916, 9843)
        spawn(2917, 9838)
        spawn(2919, 9828)
        spawn(2922, 9835)
        spawn(2927, 9830)
        spawn(2980, 9840)
    }
})
