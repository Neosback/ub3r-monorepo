package net.dodian.uber.game.npc

internal object ThrowerTroll : NpcFamily by npcFamily("Thrower Troll", 1103, block = {
    cache {
        examine = "He has a colourful personality."
    }

    server {
        deathAnimation = 2304
        hitpoints = 96
    }

    spawns {
        spawn(3229, 3108)
    }
})
