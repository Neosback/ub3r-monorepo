package net.dodian.uber.game.npc

internal object Ghost2 : NpcFamily by npcFamily("Ghost", 103, block = {
    runtime {
        deathAnimation = 2304
        respawnTicks = 30
        hitpoints = 50
    }

    spawns {
        spawn(3205, 9881)
        spawn(3209, 9878)
        spawn(3209, 9885)
        spawn(3248, 9916)
        spawn(3251, 9915)
        spawn(3256, 9916)
    }
})
