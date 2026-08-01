package net.dodian.uber.game.npc

internal object KnightOfArdougne : NpcFamily by npcFamily("Knight of Ardougne", 3297, block = {
    definition {
        name = "Knight of Ardougne"
    }

    server {
        attackAnimation = 451
        deathAnimation = 2304
        attack = 30
        strength = 40
        defence = 20
        hitpoints = 52
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2658, 3306)
        spawn(2659, 3310)
        spawn(2660, 3301)
        spawn(2662, 3309)
        spawn(2663, 3304)
        spawn(2664, 3311)
        spawn(2665, 3301)
        spawn(2667, 3306)
    }
})
