package net.dodian.uber.game.npc

internal object Skeleton : NpcFamily by npcFamily("Skeleton", 90, block = {
    cache {
        examine = "Eeek! A ghost! Grave of Scorpius:  Spooky."
    }

    server {
        defenceAnimation = 5533
        deathAnimation = 2304
        hitpoints = 29
        attack = 13
        strength = 13
        defence = 18
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3203, 9881)
    }
})
