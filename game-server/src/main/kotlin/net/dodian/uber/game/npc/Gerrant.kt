package net.dodian.uber.game.npc

internal object Gerrant : NpcFamily by npcFamily("Gerrant", 1790, block = {
    cache {
        name = "Gerrant"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2597, 3401)
        spawn(2835, 3442)
        spawn(2871, 2968)
    }
})
