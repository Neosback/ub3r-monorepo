package net.dodian.uber.game.npc

internal object Dwayne : NpcFamily by npcFamily("Dwayne", 2261, block = {
    definition {
        examine = "I am the one and only Dwayne!"
    }

    server {
        attackAnimation = 1312
        deathAnimation = 1314
        respawnTicks = 180
        attack = 120
        strength = 140
        defence = 150
        hitpoints = 340
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2778, 3208)
    }
})
