package net.dodian.uber.game.npc

internal object ShadowWarrior : NpcFamily by npcFamily("Shadow warrior", 2853, block = {
    definition {
        examine = "A fighter from the supernatural world. He's a shadow of his former self."
    }

    server {
        deathAnimation = 2304
        attack = 45
        strength = 50
        defence = 55
        hitpoints = 65
        ranged = 60
        magic = 60
    }

    spawns {
        spawn(2714, 9749)
        spawn(2719, 9754)
        spawn(2723, 9750)
        spawn(2725, 9755)
    }
})
