package net.dodian.uber.game.npc

internal object Sheep : NpcFamily by npcFamily("Sheep", 2693, block = {
    cache {
        name = "Sheep"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2458, 3431)
        spawn(2460, 3433)
        spawn(2460, 3436)
        spawn(2462, 3431)
        spawn(2462, 3434)
    }
})
