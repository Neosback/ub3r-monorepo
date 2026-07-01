package net.dodian.uber.game.npc

internal object MasterFarmer : NpcFamily by npcFamily("Master Farmer", 5730, block = {
    cache {
        name = "Master Farmer"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2559, 3102)
    }
})
