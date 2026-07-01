package net.dodian.uber.game.npc

internal object ThrowerTroll2 : NpcFamily by npcFamily("Thrower Troll", 1105, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 96
    }

    spawns {
        spawn(3589, 3477)
    }
})
