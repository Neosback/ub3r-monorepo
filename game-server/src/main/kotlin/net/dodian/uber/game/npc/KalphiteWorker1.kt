package net.dodian.uber.game.npc

internal object KalphiteWorker1 : NpcFamily by npcFamily("Kalphite worker 1", 955, block = {
    definition {
        examine = "Hard working bug"
    }

    server {
        defenceAnimation = 6219
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
        spawn(3221, 3106)
        spawn(3221, 3111)
        spawn(3224, 3103)
        spawn(3224, 3108)
        spawn(3224, 3115)
        spawn(3226, 3111)
        spawn(3228, 3103)
        spawn(3230, 3105)
        spawn(3230, 3110)
        spawn(3231, 3108)
    }
})
