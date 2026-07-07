package net.dodian.uber.game.npc

internal object Dagannoth : NpcFamily by npcFamily("Dagannoth", 1338, block = {
    definition {
        examine = "A messy bird.  Smells of rotten fish."
    }

    server {
        deathAnimation = 2304
        hitpoints = 71
        attack = 1
        strength = 1
        defence = 1
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(1842, 4494)
    }
})
