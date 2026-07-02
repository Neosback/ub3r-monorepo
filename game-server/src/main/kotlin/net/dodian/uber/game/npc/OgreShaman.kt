package net.dodian.uber.game.npc

internal object OgreShaman : NpcFamily by npcFamily("Ogre shaman", 4382, block = {
    server {
        hitpoints = 1
        attack = 1
        strength = 1
        defence = 1
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2577, 9463)
        spawn(2580, 9459)
        spawn(2581, 9465)
        spawn(2585, 9462)
        spawn(2589, 9464)
        spawn(2605, 9456)
        spawn(2608, 9461)
        spawn(2610, 9452)
        spawn(2611, 9456)
        spawn(2616, 9455)
    }
})
