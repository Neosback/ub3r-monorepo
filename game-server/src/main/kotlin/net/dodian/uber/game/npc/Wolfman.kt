package net.dodian.uber.game.npc

internal object Wolfman : NpcFamily by npcFamily("Wolfman", 3200, block = {
    definition {
        name = "Wolfman"
    }

    server {
        defenceAnimation = 221
        deathAnimation = 2304
        hitpoints = 100
    }

    spawns {
        spawn(2805, 3427)
    }
})
