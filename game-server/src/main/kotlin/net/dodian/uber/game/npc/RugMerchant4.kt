package net.dodian.uber.game.npc

internal object RugMerchant4 : NpcFamily by npcFamily("Rug merchant", 22, block = {
    cache {
        examine = "Proud owner of carpet co"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(3401, 2918)
    }
})
