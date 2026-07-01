package net.dodian.uber.game.npc

internal object GiantSpider : NpcFamily by npcFamily("Giant spider", 59, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 2
        attack = 19
        strength = 21
        defence = 16
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3211, 9894)
        spawn(3212, 9886)
        spawn(3217, 9890)
        spawn(3220, 9888)
    }
})
