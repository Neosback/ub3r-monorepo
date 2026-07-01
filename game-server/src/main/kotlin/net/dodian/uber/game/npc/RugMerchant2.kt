package net.dodian.uber.game.npc

internal object RugMerchant2 : NpcFamily by npcFamily("Rug merchant", 19, block = {
    cache {
        examine = "Proud owner of carpet co"
    }

    runtime {
        deathAnimation = 2304
        hitpoints = 52
    }

    spawns {
        spawn(3181, 3045)
    }
})
