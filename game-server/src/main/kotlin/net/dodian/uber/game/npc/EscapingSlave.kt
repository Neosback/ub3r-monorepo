package net.dodian.uber.game.npc

internal object EscapingSlave : NpcFamily by npcFamily("Escaping slave", 826, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2526, 4778)
    }
})
