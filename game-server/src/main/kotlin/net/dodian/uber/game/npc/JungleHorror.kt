package net.dodian.uber.game.npc

internal object JungleHorror : NpcFamily by npcFamily("Jungle horror", 1042, block = {
    cache {
        name = "Jungle horror"
    }

    runtime {
        attackAnimation = 4234
        deathAnimation = 4233
        attack = 60
        strength = 80
        defence = 45
        hitpoints = 52
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3724, 9382, face = SOUTH)
        spawn(3725, 9390, face = SOUTH)
        spawn(3725, 9396, face = SOUTH)
        spawn(3728, 9400, face = SOUTH)
        spawn(3729, 9382, face = SOUTH)
        spawn(3730, 9387, face = SOUTH)
        spawn(3730, 9413, face = SOUTH)
        spawn(3733, 9401, face = SOUTH)
        spawn(3734, 9394, face = SOUTH)
        spawn(3736, 9390, face = SOUTH)
        spawn(3736, 9398, face = SOUTH)
        spawn(3736, 9413, face = SOUTH)
        spawn(3738, 9404, face = SOUTH)
        spawn(3739, 9381, face = SOUTH)
        spawn(3739, 9387, face = SOUTH)
        spawn(3739, 9410, face = SOUTH)
        spawn(3742, 9406, face = SOUTH)
        spawn(3743, 9398, face = SOUTH)
        spawn(3744, 9414, face = SOUTH)
        spawn(3745, 9387, face = SOUTH)
        spawn(3746, 9409, face = SOUTH)
        spawn(3748, 9413, face = SOUTH)
        spawn(3748, 9418, face = SOUTH)
        spawn(3749, 9398, face = SOUTH)
        spawn(3750, 9387, face = SOUTH)
        spawn(3752, 9414, face = SOUTH)
        spawn(3753, 9390, face = SOUTH)
        spawn(3753, 9398, face = SOUTH)
        spawn(3754, 9386, face = SOUTH)
        spawn(3755, 9403, face = SOUTH)
        spawn(3756, 9393, face = SOUTH)
        spawn(3756, 9416, face = SOUTH)
        spawn(3757, 9407, face = SOUTH)
        spawn(3758, 9389, face = SOUTH)
        spawn(3759, 9405, face = SOUTH)
        spawn(3761, 9394, face = SOUTH)
        spawn(3762, 9403, face = SOUTH)
        spawn(3762, 9417, face = SOUTH)
        spawn(3763, 9412, face = SOUTH)
        spawn(3763, 9420, face = SOUTH)
        spawn(3764, 9408, face = SOUTH)
        spawn(3766, 9395, face = SOUTH)
        spawn(3766, 9403, face = SOUTH)
        spawn(3766, 9416, face = SOUTH)
        spawn(3768, 9399, face = SOUTH)
        spawn(3768, 9419, face = SOUTH)
    }
})
