package net.dodian.uber.game.npc

internal object AliTheHag : NpcFamily by npcFamily("Ali the Hag", 3541, block = {
    cache {
        examine = "Not trustworthy"
    }

    spawns {
        spawn(3346, 2987)
    }
})
