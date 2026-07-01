package net.dodian.uber.game.npc

internal object JalTokJad : NpcFamily by npcFamily("JalTok-Jad", 7704, block = {
    runtime {
        hitpoints = 350
        attack = 750
        strength = 1020
        defence = 480
        magic = 510
        ranged = 1020
    }

    spawns {
        spawn(3326, 3333)
    }
})
