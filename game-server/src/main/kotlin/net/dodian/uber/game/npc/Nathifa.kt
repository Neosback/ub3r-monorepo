package net.dodian.uber.game.npc

internal object Nathifa : NpcFamily by npcFamily("Nathifa", 3890, block = {
    definition {
        examine = "She sell good quality pickaxe"
    }

    server {
        attack = 1
        strength = 1
        defence = 1
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(3316, 2787)
    }
})
