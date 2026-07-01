package net.dodian.uber.game.npc

internal object BerserkBarbarianSpirit : NpcFamily by npcFamily("Berserk barbarian spirit", 5565, block = {
    runtime {
        attackAnimation = 6726
        deathAnimation = 6727
        respawnTicks = 35
        attack = 100
        strength = 90
        defence = 100
        hitpoints = 190
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2578, 9811)
        spawn(2655, 9807)
        spawn(2659, 9798)
        spawn(2659, 9806)
        spawn(2659, 9811)
        spawn(2660, 9795)
        spawn(2662, 9809)
        spawn(2663, 9799)
        spawn(2664, 9796)
        spawn(2665, 9806)
        spawn(2666, 9810)
    }
})
