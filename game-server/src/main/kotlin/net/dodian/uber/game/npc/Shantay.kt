package net.dodian.uber.game.npc

internal object Shantay : NpcFamily by npcFamily("Shantay", 4642, block = {
    definition {
        examine = "nice store for desert"
    }

    spawns {
        spawn(3311, 2802)
    }
})
