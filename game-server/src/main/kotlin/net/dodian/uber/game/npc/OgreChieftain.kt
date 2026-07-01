package net.dodian.uber.game.npc

internal object OgreChieftain : NpcFamily by npcFamily("Ogre chieftain", 4362, block = {
    runtime {
        hitpoints = 60
        attack = 75
        strength = 71
        defence = 75
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2571, 9429)
        spawn(2572, 9435)
        spawn(2575, 9432)
        spawn(2577, 9438)
        spawn(2579, 9429)
        spawn(2580, 9436)
        spawn(2580, 9442)
        spawn(2583, 9433)
        spawn(2584, 9439)
    }
})
