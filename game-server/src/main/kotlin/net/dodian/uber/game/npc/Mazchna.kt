package net.dodian.uber.game.npc

internal object Mazchna : NpcFamily by npcFamily("Mazchna", 402, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2885, 3450)
    }
})
