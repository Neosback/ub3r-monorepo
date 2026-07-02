package net.dodian.uber.game.npc

internal object Banker : NpcFamily by npcFamily("Banker", 1613, block = {
    cache {
        name = "Banker"
        examine = "I do not get paid enough for this."
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2443, 3424, z = 1)
        spawn(2443, 3425, z = 1)
        spawn(2448, 3424, z = 1)
        spawn(2448, 3427, z = 1)
        spawn(2584, 3421, face = EAST)
        spawn(2615, 3091, face = WEST)
        spawn(2615, 3092, face = WEST)
        spawn(2615, 3094, face = WEST)
        spawn(2721, 3495)
        spawn(2722, 3495)
        spawn(2724, 3495)
        spawn(2727, 3378)
        spawn(2727, 3495)
        spawn(2728, 3495)
        spawn(2729, 3495)
        spawn(2807, 3443, face = SOUTH)
        spawn(2809, 3443, face = SOUTH)
        spawn(2810, 3443, face = SOUTH)
        spawn(2811, 3443, face = SOUTH)
        spawn(2850, 2955)
        spawn(2854, 2955)
        spawn(2869, 2983, z = 1)
        spawn(2901, 3475)
        spawn(2902, 3475)
        spawn(2903, 3475)
        spawn(2904, 3475)
        spawn(2932, 4687)
        spawn(3054, 3381, face = WEST)
        spawn(3096, 3489, face = WEST)
        spawn(3096, 3491, face = WEST)
        spawn(3096, 3492)
        spawn(3098, 3492)
        spawn(3187, 3436)
        spawn(3187, 3438)
        spawn(3187, 3440)
        spawn(3187, 3442)
        spawn(3187, 3444)
        spawn(3187, 3446)
        spawn(3252, 3418)
        spawn(3253, 3418)
        spawn(3254, 3418)
        spawn(3259, 2780)
        spawn(3514, 3479, face = WEST)
        spawn(3514, 3481, face = WEST)
        spawn(3514, 3483, face = WEST)
    }
})
