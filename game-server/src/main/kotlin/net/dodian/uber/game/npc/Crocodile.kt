package net.dodian.uber.game.npc

internal object Crocodile : NpcFamily by npcFamily("Crocodile", 4184, block = {
    definition {
        examine = "Ow Charlie that hurts."
    }

    server {
        attackAnimation = 2039
        deathAnimation = 2038
        attack = 65
        strength = 70
        defence = 55
        hitpoints = 78
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3272, 2861)
        spawn(3273, 2864)
        spawn(3276, 2866)
        spawn(3279, 2866)
        spawn(3282, 2865)
        spawn(3284, 2862)
        spawn(3325, 2917)
        spawn(3329, 2915)
        spawn(3330, 2919)
        spawn(3333, 2917)
        spawn(3338, 2914)
        spawn(3338, 2919)
        spawn(3338, 2923)
        spawn(3341, 2921)
        spawn(3344, 2918)
        spawn(3345, 2921)
        spawn(3348, 2923)
        spawn(3349, 2918)
        spawn(3351, 2920)
    }
})
