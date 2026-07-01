package net.dodian.uber.game.npc

internal object AlbinoBat : NpcFamily by npcFamily("Albino bat", 1039, block = {
    cache {
        name = "Albino bat"
    }

    runtime {
        attackAnimation = 4915
        deathAnimation = 4917
        respawnTicks = 30
        attack = 50
        strength = 60
        defence = 35
        hitpoints = 43
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3718, 9352, face = SOUTH_EAST)
        spawn(3719, 9357, face = SOUTH_EAST)
        spawn(3721, 9349, face = SOUTH_EAST)
        spawn(3722, 9362, face = SOUTH_EAST)
        spawn(3722, 9368, face = SOUTH_EAST)
        spawn(3724, 9356, face = SOUTH_EAST)
        spawn(3724, 9377, face = SOUTH_EAST)
        spawn(3725, 9350, face = SOUTH_EAST)
        spawn(3726, 9373, face = SOUTH_EAST)
        spawn(3727, 9370, face = SOUTH_EAST)
        spawn(3728, 9361, face = SOUTH_EAST)
        spawn(3728, 9366, face = SOUTH_EAST)
        spawn(3730, 9353, face = SOUTH_EAST)
        spawn(3730, 9357, face = SOUTH_EAST)
        spawn(3731, 9350, face = SOUTH_EAST)
        spawn(3731, 9368, face = SOUTH_EAST)
        spawn(3731, 9377, face = SOUTH_EAST)
        spawn(3734, 9351, face = SOUTH_EAST)
        spawn(3734, 9355, face = SOUTH_EAST)
        spawn(3734, 9365, face = SOUTH_EAST)
        spawn(3736, 9372, face = SOUTH_EAST)
        spawn(3736, 9377, face = SOUTH_EAST)
        spawn(3738, 9350, face = SOUTH_EAST)
        spawn(3738, 9356, face = SOUTH_EAST)
        spawn(3738, 9365, face = SOUTH_EAST)
        spawn(3739, 9375, face = SOUTH_EAST)
        spawn(3741, 9353, face = SOUTH_EAST)
    }
})
