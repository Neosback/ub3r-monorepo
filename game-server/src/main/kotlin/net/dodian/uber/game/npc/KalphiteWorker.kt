package net.dodian.uber.game.npc

internal object KalphiteWorker : NpcFamily by npcFamily("Kalphite Worker", 1153, block = {
    definition {
        examine = "Big, ugly, and smelly. / A large dim looking humanoid."
    }

    server {
        attackAnimation = 6224
        deathAnimation = 6228
        respawnTicks = 30
        attack = 15
        strength = 15
        defence = 10
        hitpoints = 22
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3486, 9526, z = 2)
        spawn(3489, 9510, z = 2)
        spawn(3490, 9524, z = 2)
        spawn(3494, 9509, z = 2)
        spawn(3496, 9525, z = 2)
        spawn(3498, 9511, z = 2)
        spawn(3498, 9517, z = 2)
        spawn(3501, 9523, z = 2)
        spawn(3503, 9518, z = 2)
        spawn(3505, 9526, z = 2)
        spawn(3508, 9519, z = 2)
        spawn(3509, 9524, z = 2)
    }
})
