package net.dodian.uber.game.npc

internal object Vannaka : NpcFamily by npcFamily("Vannaka", 403, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2798, 3441)
    }
})
