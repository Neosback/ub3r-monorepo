package net.dodian.uber.game.npc

internal object Bloodveld : NpcFamily by npcFamily("Bloodveld", 484, block = {
    runtime {
        attackAnimation = 1552
        deathAnimation = 1553
        respawnTicks = 35
        hitpoints = 120
        attack = 75
        strength = 45
        defence = 30
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2643, 9847)
        spawn(2648, 9843)
        spawn(2652, 9843)
        spawn(2657, 9843)
        spawn(2661, 9843)
        spawn(2664, 9852)
        spawn(2666, 9842)
        spawn(2666, 9850)
    }
})
