package net.dodian.uber.game.npc

internal object Jackal : NpcFamily by npcFamily("Jackal", 4185, block = {
    cache {
        examine = "Man's best friend"
    }

    runtime {
        attackAnimation = 6559
        deathAnimation = 6558
        attack = 25
        strength = 34
        defence = 30
        hitpoints = 39
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3388, 2946)
        spawn(3391, 2943)
        spawn(3391, 2948)
        spawn(3394, 2944)
        spawn(3395, 2947)
        spawn(3398, 2946)
        spawn(3398, 2950)
        spawn(3400, 2952)
        spawn(3401, 2946)
        spawn(3403, 2950)
    }
})
