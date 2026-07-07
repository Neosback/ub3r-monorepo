package net.dodian.uber.game.npc

internal object RugMerchant2 : NpcFamily by npcFamily("Rug merchant", 19, block = {
    definition {
        examine = "Proud owner of carpet co"
    }

    server {
        deathAnimation = 2304
        hitpoints = 52
    }

    options {
        talkTo(handler = ::handleRugMerchantTalkTo)
    }

    spawns {
        spawn(3181, 3045)
    }
})
