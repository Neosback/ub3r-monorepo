package net.dodian.uber.game.npc

internal object Chicken : NpcFamily by npcFamily("Chicken", 3661, block = {
    runtime {
        attackAnimation = 5387
        deathAnimation = 5389
        respawnTicks = 20
        attack = 1
        strength = 1
        hitpoints = 3
        defence = 1
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2677, 3313)
        spawn(2677, 3317)
        spawn(2678, 3315)
        spawn(2679, 3311)
        spawn(2680, 3317)
        spawn(2682, 3312)
        spawn(2682, 3316)
        spawn(2683, 3314)
    }
})
