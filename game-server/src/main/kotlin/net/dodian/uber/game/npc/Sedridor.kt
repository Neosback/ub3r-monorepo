package net.dodian.uber.game.npc

internal object Sedridor : NpcFamily by npcFamily("Sedridor", 5034, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2590, 3086, face = EAST)
    }
})
