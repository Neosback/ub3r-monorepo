package net.dodian.uber.game.npc

internal object Farmer : NpcFamily by npcFamily("Farmer", 3086, block = {
    cache {
        examine = "He grows the crops in this area."
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2590, 3102, face = SOUTH)
    }
})
