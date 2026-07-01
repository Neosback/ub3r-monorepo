package net.dodian.uber.game.npc

internal object Ian : NpcFamily by npcFamily("Ian", 1779, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(1934, 4458, z = 2)
    }
})
