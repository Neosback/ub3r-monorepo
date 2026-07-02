package net.dodian.uber.game.npc

internal object CaveBugLarva : NpcFamily by npcFamily("Cave bug larva", 1833, block = {
    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2463, 4365)
    }
})
