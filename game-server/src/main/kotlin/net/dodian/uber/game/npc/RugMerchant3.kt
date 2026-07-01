package net.dodian.uber.game.npc

internal object RugMerchant3 : NpcFamily by npcFamily("Rug merchant", 20, block = {
    cache {
        examine = "Proud owner of carpet co"
    }

    runtime {
        deathAnimation = 2304
        respawnTicks = 10
        hitpoints = 99
    }

    spawns {
        spawn(3348, 3002)
    }
})
