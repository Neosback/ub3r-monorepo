package net.dodian.uber.game.npc

internal object GreaterDemon : NpcFamily by npcFamily("Greater demon", 2025, block = {
    runtime {
        attackAnimation = 64
        deathAnimation = 67
        respawnTicks = 35
        attack = 95
        strength = 99
        defence = 95
        hitpoints = 166
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2895, 9796)
        spawn(2895, 9800)
        spawn(2898, 9798)
        spawn(2898, 9803)
        spawn(2902, 9802)
        spawn(2904, 9799)
        spawn(2909, 9798)
        spawn(2912, 9809)
        spawn(2914, 9801)
        spawn(2916, 9804)
    }
})
