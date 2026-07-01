package net.dodian.uber.game.npc

internal object LesserDemon : NpcFamily by npcFamily("Lesser demon", 2005, block = {
    runtime {
        attackAnimation = 64
        deathAnimation = 67
        respawnTicks = 35
        attack = 80
        strength = 90
        defence = 90
        hitpoints = 110
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2915, 9758)
        spawn(2919, 9757)
        spawn(2921, 9760)
        spawn(2925, 9763)
        spawn(2925, 9766)
        spawn(2928, 9761)
        spawn(2928, 9804)
        spawn(2931, 9795)
        spawn(2933, 9810)
        spawn(2937, 9793)
    }
})
