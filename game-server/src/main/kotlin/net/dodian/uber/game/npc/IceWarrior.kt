package net.dodian.uber.game.npc

internal object IceWarrior : NpcFamily by npcFamily("Ice warrior", 2851, block = {
    cache {
        examine = "A cold-hearted elemental warrior."
    }

    server {
        deathAnimation = 843
        hitpoints = 60
        attack = 47
        strength = 47
        defence = 47
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2695, 9836)
        spawn(2698, 9837)
        spawn(2700, 9841)
        spawn(2706, 9842)
        spawn(2709, 9841)
        spawn(2711, 9844)
        spawn(2832, 3512)
        spawn(2835, 3508)
        spawn(2837, 3506)
        spawn(2839, 3502)
        spawn(2842, 3504)
        spawn(2851, 3509)
        spawn(2855, 3510)
    }
})
