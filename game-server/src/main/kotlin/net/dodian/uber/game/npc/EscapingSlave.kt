package net.dodian.uber.game.npc

internal object EscapingSlave : NpcFamily by npcFamily("Escaping slave", 826, block = {
    cache {
        examine = "His job is to keep the ship in tip-top condition."
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2526, 4778)
    }
})
