package net.dodian.uber.game.npc

internal object Zombie : NpcFamily by npcFamily("Zombie", 56, block = {
    runtime {
        deathAnimation = 2304
        respawnTicks = 1
        hitpoints = 1
        attack = 19
        strength = 21
        defence = 16
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3229, 3214)
    }
})
