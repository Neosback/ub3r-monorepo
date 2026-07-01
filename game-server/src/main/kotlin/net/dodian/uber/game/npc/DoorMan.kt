package net.dodian.uber.game.npc

internal object DoorMan : NpcFamily by npcFamily("Door man", 33, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 22
        attack = 8
        strength = 9
        defence = 10
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2763, 3507)
    }
})
