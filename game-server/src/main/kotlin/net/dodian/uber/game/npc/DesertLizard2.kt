package net.dodian.uber.game.npc

internal object DesertLizard2 : NpcFamily by npcFamily("Desert lizard", 461, block = {
    cache {
        examine = "how can it survive the heat?"
    }

    runtime {
        attackAnimation = 2776
        deathAnimation = 2778
        respawnTicks = 50
        attack = 22
        strength = 22
        defence = 22
        hitpoints = 33
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3422, 3041)
        spawn(3422, 3045)
        spawn(3424, 3038)
        spawn(3425, 3050)
        spawn(3426, 3039)
        spawn(3426, 3045)
        spawn(3429, 3039)
        spawn(3429, 3043)
        spawn(3429, 3048)
        spawn(3433, 3042)
        spawn(3434, 3046)
        spawn(3434, 3050)
        spawn(3436, 3039)
    }
})
