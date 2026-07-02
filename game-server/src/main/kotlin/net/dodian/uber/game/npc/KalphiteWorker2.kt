package net.dodian.uber.game.npc

internal object KalphiteWorker2 : NpcFamily by npcFamily("Kalphite worker 2", 956, block = {
    cache {
        examine = "Hard working bug"
    }

    server {
        attackAnimation = 6223
        deathAnimation = 6228
        respawnTicks = 40
        attack = 20
        strength = 25
        defence = 20
        hitpoints = 35
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3370, 2902)
        spawn(3370, 2905)
        spawn(3371, 2909)
        spawn(3372, 2897)
        spawn(3373, 2912)
        spawn(3374, 2898)
        spawn(3375, 2902)
        spawn(3375, 2909)
        spawn(3377, 2900)
        spawn(3377, 2904)
        spawn(3379, 2908)
    }
})
