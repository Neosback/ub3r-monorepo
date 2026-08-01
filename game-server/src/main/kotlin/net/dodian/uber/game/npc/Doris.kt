package net.dodian.uber.game.npc

internal object Doris : NpcFamily by npcFamily("Doris", 4808, block = {
    definition {
        examine = "How did she obtain battlestaffs?"
    }

    spawns {
        spawn(3080, 3509)
    }
})
