package net.dodian.uber.game.npc

internal object Horvik : NpcFamily by npcFamily("Horvik", 2882, block = {
    cache {
        name = "Horvik"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2615, 3083)
    }
})
