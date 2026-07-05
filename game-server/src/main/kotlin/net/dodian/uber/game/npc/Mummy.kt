package net.dodian.uber.game.npc

internal object Mummy : NpcFamily by npcFamily("Mummy", 950, block = {
    cache {
        examine = "What a short sworded mummy"
    }

    server {
        defenceAnimation = 5552
        attackAnimation = 5554
        deathAnimation = 5555
        respawnTicks = 40
        attack = 75
        strength = 90
        defence = 75
        hitpoints = 100
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2575, 9500)
        spawn(2577, 9498)
        spawn(2577, 9502)
        spawn(2579, 9500)
        spawn(2579, 9504)
        spawn(2580, 9497)
        spawn(2581, 9503)
        spawn(2582, 9499)
        spawn(2583, 9501)
        spawn(2584, 9497)
        spawn(2586, 9500)
        spawn(2588, 9498)
    }
})
