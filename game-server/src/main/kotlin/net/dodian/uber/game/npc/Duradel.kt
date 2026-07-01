package net.dodian.uber.game.npc

internal object Duradel : NpcFamily by npcFamily("Duradel", 405, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2606, 3398)
    }
})
