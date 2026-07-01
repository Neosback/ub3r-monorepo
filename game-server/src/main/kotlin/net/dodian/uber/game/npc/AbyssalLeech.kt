package net.dodian.uber.game.npc

internal object AbyssalLeech : NpcFamily by npcFamily("Abyssal leech", 2584, block = {
    runtime {
        attackAnimation = 2181
        deathAnimation = 2183
        respawnTicks = 50
        attack = 50
        strength = 50
        defence = 30
        hitpoints = 35
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2903, 4841)
        spawn(2905, 4826)
        spawn(2906, 4839)
        spawn(2907, 4837)
        spawn(2908, 4826)
        spawn(2909, 4829)
        spawn(2909, 4834)
        spawn(2910, 4831)
        spawn(2911, 4833)
        spawn(2913, 4831)
        spawn(2913, 4834)
        spawn(2914, 4837)
        spawn(2915, 4826)
        spawn(2915, 4830)
        spawn(2916, 4839)
        spawn(2917, 4823)
        spawn(2919, 4842)
        spawn(2920, 4823)
        spawn(2920, 4840)
    }
})
