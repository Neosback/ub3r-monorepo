package net.dodian.uber.game.npc

internal object BlueDragon : NpcFamily by npcFamily("Blue dragon", 265, block = {
    cache {
        examine = "Blue eyes nerf dragon"
    }

    runtime {
        attackAnimation = 91
        deathAnimation = 92
        respawnTicks = 40
        attack = 90
        strength = 95
        defence = 90
        hitpoints = 110
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3229, 9368)
        spawn(3234, 9370, face = SOUTH_EAST)
        spawn(3236, 9374)
        spawn(3237, 9387)
        spawn(3238, 9380)
        spawn(3239, 9385)
        spawn(3242, 9371)
        spawn(3242, 9383)
        spawn(3247, 9360)
        spawn(3247, 9368)
    }
})
