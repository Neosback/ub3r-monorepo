package net.dodian.uber.game.npc

internal object BabaYaga : NpcFamily by npcFamily("Baba Yaga", 3837, block = {
    cache {
        examine = "Mysterious woman"
    }

    spawns {
        spawn(2461, 3428)
    }
})
