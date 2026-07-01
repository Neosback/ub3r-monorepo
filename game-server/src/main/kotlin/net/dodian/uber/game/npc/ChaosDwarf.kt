package net.dodian.uber.game.npc

internal object ChaosDwarf : NpcFamily by npcFamily("Chaos dwarf", 291, block = {
    runtime {
        attackAnimation = 99
        deathAnimation = 102
        respawnTicks = 35
        attack = 43
        strength = 43
        defence = 43
        hitpoints = 62
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2955, 9775)
        spawn(2955, 9794)
        spawn(2961, 9780)
        spawn(2961, 9785)
        spawn(2966, 9775)
        spawn(2966, 9794)
    }
})
