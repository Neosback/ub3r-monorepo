package net.dodian.uber.game.npc

internal object Sheep : NpcFamily by npcFamily("Sheep", 2693, block = {
    definition {
        examine = "Yep. Definitely a chicken."
        name = "Sheep"
    }

    server {
        defenceAnimation = 5388
        deathAnimation = 2304
    }

    spawns {
        spawn(2458, 3431)
        spawn(2460, 3433)
        spawn(2460, 3436)
        spawn(2462, 3431)
        spawn(2462, 3434)
    }
})
