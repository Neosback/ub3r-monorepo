package net.dodian.uber.game.npc

internal object Zogre2 : NpcFamily by npcFamily("Zogre", 2053, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 72
    }

    spawns {
        spawn(2652, 3294)
    }
})
