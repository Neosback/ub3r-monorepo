package net.dodian.uber.game.npc

internal object Goat : NpcFamily by npcFamily("Goat", 1795, block = {
    cache {
        examine = "Thinks Billy is retarded"
    }

    runtime {
        attackAnimation = 250
        deathAnimation = 253
        respawnTicks = 50
        attack = 20
        strength = 20
        defence = 20
        hitpoints = 23
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3157, 3043)
        spawn(3158, 3045)
        spawn(3159, 3041)
        spawn(3160, 3046)
        spawn(3162, 3041)
        spawn(3162, 3045)
        spawn(3163, 3043)
    }
})
