package net.dodian.uber.game.npc

internal object CaveHorror : NpcFamily by npcFamily("Cave horror", 1047, block = {
    definition {
        name = "Cave horror"
        examine = "A horrible, emaciated ape like creature with beady red eyes."
    }

    server {
        attackAnimation = 4234
        deathAnimation = 4233
        attack = 80
        strength = 95
        defence = 55
        hitpoints = 70
        ranged = 140
        magic = 80
    }

    spawns {
        spawn(3718, 9419, face = SOUTH)
        spawn(3718, 9425, face = SOUTH)
        spawn(3720, 9430, face = SOUTH)
        spawn(3721, 9436, face = SOUTH)
        spawn(3722, 9427, face = SOUTH)
        spawn(3723, 9439, face = SOUTH)
        spawn(3724, 9418, face = SOUTH)
        spawn(3725, 9442, face = SOUTH)
        spawn(3726, 9448, face = SOUTH)
        spawn(3726, 9458, face = SOUTH)
        spawn(3727, 9439, face = SOUTH)
        spawn(3728, 9426, face = SOUTH)
        spawn(3728, 9453, face = SOUTH)
        spawn(3730, 9420, face = SOUTH)
        spawn(3730, 9463, face = SOUTH)
        spawn(3731, 9430, face = SOUTH)
        spawn(3731, 9441, face = SOUTH)
        spawn(3732, 9458, face = SOUTH)
        spawn(3733, 9454, face = SOUTH)
        spawn(3734, 9443, face = SOUTH)
        spawn(3735, 9423, face = SOUTH)
        spawn(3735, 9462, face = SOUTH)
        spawn(3736, 9431, face = SOUTH)
        spawn(3737, 9441, face = SOUTH)
        spawn(3737, 9446, face = SOUTH)
        spawn(3738, 9436, face = SOUTH)
        spawn(3738, 9451, face = SOUTH)
        spawn(3739, 9454, face = SOUTH)
        spawn(3740, 9425, face = SOUTH)
        spawn(3741, 9429, face = SOUTH)
        spawn(3743, 9462, face = SOUTH)
        spawn(3744, 9424, face = SOUTH)
        spawn(3745, 9436, face = SOUTH)
        spawn(3746, 9451, face = SOUTH)
        spawn(3748, 9422, face = SOUTH)
        spawn(3749, 9446, face = SOUTH)
        spawn(3749, 9460, face = SOUTH)
        spawn(3750, 9428, face = SOUTH)
        spawn(3751, 9437, face = SOUTH)
        spawn(3751, 9451, face = SOUTH)
        spawn(3751, 9455, face = SOUTH)
        spawn(3753, 9425, face = SOUTH)
        spawn(3754, 9439, face = SOUTH)
        spawn(3754, 9446, face = SOUTH)
        spawn(3754, 9459, face = SOUTH)
        spawn(3758, 9428, face = SOUTH)
        spawn(3758, 9439, face = SOUTH)
        spawn(3758, 9446, face = SOUTH)
        spawn(3758, 9457, face = SOUTH)
        spawn(3759, 9423, face = SOUTH)
        spawn(3760, 9442, face = SOUTH)
        spawn(3761, 9453, face = SOUTH)
        spawn(3762, 9439, face = SOUTH)
        spawn(3762, 9449, face = SOUTH)
        spawn(3763, 9431, face = SOUTH)
        spawn(3764, 9426, face = SOUTH)
        spawn(3766, 9436, face = SOUTH)
        spawn(3768, 9423, face = SOUTH)
    }
})
