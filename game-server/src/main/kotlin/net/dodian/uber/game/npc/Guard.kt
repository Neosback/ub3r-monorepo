package net.dodian.uber.game.npc

internal object Guard : NpcFamily by npcFamily("Guard", 32, block = {
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
        spawn(3242, 9916)
        spawn(3244, 9915)
    }
})
