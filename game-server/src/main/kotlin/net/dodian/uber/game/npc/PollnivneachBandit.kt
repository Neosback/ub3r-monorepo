package net.dodian.uber.game.npc

internal object PollnivneachBandit : NpcFamily by npcFamily("Pollnivneach bandit", 736, block = {
    cache {
        examine = "Just following orders"
    }

    runtime {
        attackAnimation = 401
        deathAnimation = 2304
        attack = 35
        strength = 35
        defence = 35
        hitpoints = 42
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3354, 2975)
        spawn(3356, 2978)
        spawn(3356, 2988)
        spawn(3359, 2975)
        spawn(3360, 2987)
        spawn(3361, 2995)
        spawn(3362, 2990)
        spawn(3363, 2977)
        spawn(3364, 2982)
        spawn(3364, 2988)
        spawn(3365, 2996)
        spawn(3367, 2979)
        spawn(3369, 2988)
        spawn(3369, 2996)
        spawn(3371, 2994)
    }
})
