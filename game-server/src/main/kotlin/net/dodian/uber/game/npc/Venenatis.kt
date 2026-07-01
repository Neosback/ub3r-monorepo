package net.dodian.uber.game.npc

internal object Venenatis : NpcFamily by npcFamily("Venenatis", 6610, block = {
    cache {
        examine = "Wild spider is wild"
    }

    runtime {
        attackAnimation = 5322
        deathAnimation = 5329
        respawnTicks = 250
        attack = 160
        strength = 170
        defence = 250
        hitpoints = 900
        ranged = 1
        magic = 666
    }

    spawns {
        spawn(3220, 9933)
    }
})
