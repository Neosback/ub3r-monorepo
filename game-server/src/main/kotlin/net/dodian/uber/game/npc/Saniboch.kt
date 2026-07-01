package net.dodian.uber.game.npc

internal object Saniboch : NpcFamily by npcFamily("Saniboch", 2345, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2743, 3150)
    }
})
