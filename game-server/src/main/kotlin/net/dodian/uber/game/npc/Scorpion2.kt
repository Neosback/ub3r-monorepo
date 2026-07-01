package net.dodian.uber.game.npc

internal object Scorpion2 : NpcFamily by npcFamily("Scorpion", 3024, block = {
    cache {
        examine = "Has a nice sting to it"
    }

    runtime {
        attackAnimation = 6261
        deathAnimation = 6260
        respawnTicks = 40
        attack = 10
        strength = 14
        defence = 10
        hitpoints = 22
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3310, 2869)
        spawn(3310, 2873)
        spawn(3313, 2866)
        spawn(3313, 2875)
        spawn(3314, 2870)
        spawn(3315, 2864)
        spawn(3317, 2867)
        spawn(3319, 2864)
        spawn(3321, 2869)
        spawn(3323, 2866)
    }
})
