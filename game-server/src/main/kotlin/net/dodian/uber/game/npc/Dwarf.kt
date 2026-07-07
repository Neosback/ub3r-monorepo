package net.dodian.uber.game.npc

internal object Dwarf : NpcFamily by npcFamily("Dwarf", 290, block = {
    definition {
        examine = "A dwarven worker.  A short angry guy."
    }

    server {
        defenceAnimation = 100
        attackAnimation = 99
        deathAnimation = 102
        respawnTicks = 30
        attack = 8
        strength = 15
        defence = 5
        hitpoints = 10
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2623, 3127)
        spawn(2624, 3123)
        spawn(2625, 3125)
        spawn(2625, 3128)
        spawn(2627, 3127)
        spawn(2628, 3123)
        spawn(3243, 9867)
    }
})
