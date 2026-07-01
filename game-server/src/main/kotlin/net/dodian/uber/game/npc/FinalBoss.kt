package net.dodian.uber.game.npc

internal object FinalBoss : NpcFamily by npcFamily("Final Boss", 47, block = {
    runtime {
        attackAnimation = 2705
        deathAnimation = 2707
        strength = 1
        hitpoints = 2
        attack = 13
        defence = 18
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3172, 9884)
        spawn(3177, 9895)
        spawn(3178, 9881)
        spawn(3184, 9891)
        spawn(3210, 9903)
        spawn(3210, 9910)
        spawn(3224, 9907)
        spawn(3227, 9905)
        spawn(3231, 9908)
        spawn(3236, 9867)
    }
})
