package net.dodian.uber.game.npc

internal object RugMerchant3 : NpcFamily by npcFamily("Rug merchant", 20, block = {
    definition {
        examine = "Proud owner of carpet co"
    }

    server {
        deathAnimation = 2304
        respawnTicks = 10
        hitpoints = 99
    }

    options {
        talkTo(handler = ::handleRugMerchantTalkTo)
    }

    spawns {
        spawn(3348, 3002)
    }
})
