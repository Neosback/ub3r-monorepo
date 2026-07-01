package net.dodian.uber.game.npc

internal object MithrilDragon : NpcFamily by npcFamily("Mithril Dragon", 2919, block = {
    cache {
        examine = "Dwarfs would love this dragon"
    }

    runtime {
        attackAnimation = 91
        deathAnimation = 92
        respawnTicks = 40
        attack = 200
        strength = 230
        defence = 220
        hitpoints = 250
        magic = 168
        ranged = 168
    }

    spawns {
        spawn(2656, 9828)
        spawn(2660, 9820)
        spawn(2661, 9823)
        spawn(2665, 9832)
        spawn(2666, 9825)
    }
})
