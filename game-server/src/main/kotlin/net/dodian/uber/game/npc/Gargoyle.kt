package net.dodian.uber.game.npc

internal object Gargoyle : NpcFamily by npcFamily("Gargoyle", 412, block = {
    definition {
        examine = "Flies like a rock."
    }

    server {
        defenceAnimation = 1519
        attackAnimation = 1519
        deathAnimation = 1518
        respawnTicks = 35
        attack = 95
        strength = 95
        defence = 70
        hitpoints = 150
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2640, 9855)
        spawn(2640, 9856)
        spawn(2641, 9851)
        spawn(2644, 9851)
        spawn(2645, 9845)
        spawn(2645, 9856)
        spawn(2646, 9848)
        spawn(2646, 9851)
        spawn(2648, 9852)
        spawn(2649, 9855)
    }
})
