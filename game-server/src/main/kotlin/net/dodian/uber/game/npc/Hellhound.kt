package net.dodian.uber.game.npc

internal object Hellhound : NpcFamily by npcFamily("Hellhound", 104, block = {
    runtime {
        attackAnimation = 6559
        deathAnimation = 6558
        respawnTicks = 40
        attack = 90
        strength = 90
        defence = 90
        hitpoints = 116
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2702, 9742)
        spawn(2702, 9748)
        spawn(2705, 9745)
        spawn(2705, 9752)
        spawn(9705, 9740)
    }
})
