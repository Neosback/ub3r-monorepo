package net.dodian.uber.game.npc

internal object Zahur : NpcFamily by npcFamily("Zahur", 4753, block = {
    cache {
        examine = "Like to mix with herblore"
    }

    spawns {
        spawn(3424, 2908)
    }
})
