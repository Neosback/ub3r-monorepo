package net.dodian.uber.game.npc

internal object SanTojalon : NpcFamily by npcFamily("San Tojalon", 3964, block = {
    server {
        attackAnimation = 5487
        deathAnimation = 5492
        respawnTicks = 180
        attack = 130
        strength = 120
        defence = 150
        hitpoints = 295
    }

    spawns {
        spawn(2614, 9522)
    }
})
