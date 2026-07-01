package net.dodian.uber.game.npc

internal object Bloodveld2 : NpcFamily by npcFamily("Bloodveld", 1618, block = {
    cache {
        name = "Bloodveld"
    }

    runtime {
        deathAnimation = 2304
        respawnTicks = 30
        attack = 90
        strength = 120
        defence = 70
        hitpoints = 180
    }

    spawns {
        spawn(2448, 4359)
        spawn(2452, 4365)
        spawn(2454, 4361)
        spawn(3174, 3028)
    }
})
