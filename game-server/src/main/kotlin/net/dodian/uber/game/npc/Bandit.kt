package net.dodian.uber.game.npc

internal object Bandit : NpcFamily by npcFamily("Bandit", 695, block = {
    cache {
        examine = "They do not like gods"
    }

    server {
        attackAnimation = 386
        deathAnimation = 2304
        respawnTicks = 66
        attack = 66
        strength = 66
        defence = 66
        hitpoints = 66
        ranged = 66
        magic = 66
    }

    spawns {
        spawn(3158, 2977)
        spawn(3160, 2979)
        spawn(3162, 2987)
        spawn(3163, 2980)
        spawn(3163, 2984)
        spawn(3166, 2987)
        spawn(3168, 2979)
        spawn(3169, 2982)
        spawn(3169, 2989)
        spawn(3170, 2976)
        spawn(3171, 2986)
        spawn(3172, 2974)
        spawn(3172, 2978)
        spawn(3173, 2983)
        spawn(3174, 2971)
        spawn(3175, 2974)
        spawn(3175, 2980)
        spawn(3177, 2978)
        spawn(3180, 2980)
        spawn(3181, 2983)
        spawn(3184, 2985)
        spawn(3185, 2982)
        spawn(3187, 2985)
    }
})
