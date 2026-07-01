package net.dodian.uber.game.npc

internal object Tanner : NpcFamily by npcFamily("Tanner", 5809, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2711, 3478)
    }
})
