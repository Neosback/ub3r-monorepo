package net.dodian.uber.game.npc

internal object JungleDemon : NpcFamily by npcFamily("Jungle Demon", 1443, block = {
    cache {
        examine = "I am the master of magic!"
    }

    server {
        defenceAnimation = 65
        attackAnimation = 64
        deathAnimation = 67
        respawnTicks = 180
        attack = 150
        strength = 170
        defence = 160
        hitpoints = 350
        magic = 800
        ranged = 1
    }

    spawns {
        spawn(2572, 9530)
    }
})
