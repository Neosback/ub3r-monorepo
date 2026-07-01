package net.dodian.uber.game.npc

internal object RugMerchant : NpcFamily by npcFamily("Rug merchant", 17, block = {
    cache {
        examine = "Proud owner of carpet co"
    }

    runtime {
        deathAnimation = 2304
        hitpoints = 18
    }

    spawns {
        spawn(3287, 2814)
    }
})
