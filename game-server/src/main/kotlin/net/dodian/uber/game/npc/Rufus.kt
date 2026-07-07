package net.dodian.uber.game.npc

internal object Rufus : NpcFamily by npcFamily("Rufus", 6478, block = {
    definition {
        examine = "I am Rufus"
    }

    spawns {
        spawn(3507, 3496)
    }
})
