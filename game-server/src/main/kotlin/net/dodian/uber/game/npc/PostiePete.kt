package net.dodian.uber.game.npc

internal object PostiePete : NpcFamily by npcFamily("Postie Pete", 3805, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(3434, 3434)
    }
})
