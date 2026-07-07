package net.dodian.uber.game.npc

internal object Zahra : NpcFamily by npcFamily("Zahra", 4752, block = {
    definition {
        examine = "Somewhat distance person"
    }

    spawns {
        spawn(3414, 2909)
    }
})
