package net.dodian.uber.game.npc

internal object PeeHat : NpcFamily by npcFamily("Pee hat", 927, block = {
    cache {
        examine = "Wish he was a party hat"
    }

    runtime {
        deathAnimation = 2304
        hitpoints = 120
        attack = 50
        strength = 100
        defence = 50
    }

    spawns {
        spawn(2631, 3102)
    }
})
