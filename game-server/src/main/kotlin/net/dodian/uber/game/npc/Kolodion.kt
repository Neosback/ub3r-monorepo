package net.dodian.uber.game.npc

internal object Kolodion : NpcFamily by npcFamily("Kolodion", 910, block = {
    server {
        deathAnimation = 2304
        respawnTicks = 300
        hitpoints = 400
    }

    spawns {
        spawn(2906, 9680)
    }
})
