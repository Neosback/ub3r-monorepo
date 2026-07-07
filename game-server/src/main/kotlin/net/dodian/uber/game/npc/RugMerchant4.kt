package net.dodian.uber.game.npc

internal object RugMerchant4 : NpcFamily by npcFamily("Rug merchant", 22, block = {
    definition {
        examine = "Proud owner of carpet co"
    }

    server {
        deathAnimation = 2304
    }

    options {
        talkTo(handler = ::handleRugMerchantTalkTo)
    }

    spawns {
        spawn(3401, 2918)
    }
})
