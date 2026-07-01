package net.dodian.uber.game.npc

internal object TzHaarXil : NpcFamily by npcFamily("TzHaar-Xil", 2167, block = {
    runtime {
        attackAnimation = 2611
        deathAnimation = 2607
        attack = 130
        strength = 100
        defence = 100
        hitpoints = 135
        magic = 40
        ranged = 120
    }

    spawns {
        spawn(2440, 5142)
        spawn(2441, 5135)
        spawn(2441, 5146)
        spawn(2445, 5139)
        spawn(2445, 5144)
        spawn(2449, 5146)
        spawn(2451, 5140)
        spawn(2453, 5146)
        spawn(2454, 5134)
        spawn(2456, 5143)
        spawn(2459, 5137)
        spawn(2461, 5142)
    }
})
