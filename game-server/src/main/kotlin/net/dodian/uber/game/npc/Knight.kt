package net.dodian.uber.game.npc

internal object Knight : NpcFamily by npcFamily("Knight", 5793, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(3042, 3378)
        spawn(3043, 3378)
        spawn(3044, 3378)
        spawn(3045, 3378)
        spawn(3046, 3378)
        spawn(3047, 3378)
        spawn(3048, 3378)
        spawn(3049, 3378)
    }
})
