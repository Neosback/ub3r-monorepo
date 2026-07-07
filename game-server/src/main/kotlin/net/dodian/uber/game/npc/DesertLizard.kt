package net.dodian.uber.game.npc

internal object DesertLizard : NpcFamily by npcFamily("Desert lizard", 459, block = {
    definition {
        examine = "how can it survive the heat?"
    }

    server {
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
        spawn(3405, 3039)
        spawn(3405, 3043)
        spawn(3408, 3037)
        spawn(3408, 3041)
        spawn(3409, 3045)
        spawn(3411, 3038)
        spawn(3412, 3043)
        spawn(3412, 3048)
        spawn(3414, 3041)
        spawn(3415, 3047)
        spawn(3416, 3039)
        spawn(3418, 3043)
        spawn(3419, 3048)
        spawn(3420, 3038)
    }
})
