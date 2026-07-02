package net.dodian.uber.game.npc

internal object Wolfman : NpcFamily by npcFamily("Wolfman", 3200, block = {
    cache {
        name = "Wolfman"
    }

    server {
        deathAnimation = 2304
        hitpoints = 100
    }

    spawns {
        spawn(2805, 3427)
    }
})
