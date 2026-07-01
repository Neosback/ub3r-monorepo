package net.dodian.uber.game.npc

internal object Druid : NpcFamily by npcFamily("Druid", 3098, block = {
    runtime {
        deathAnimation = 2304
        respawnTicks = 20
        attack = 30
        strength = 15
        defence = 15
        hitpoints = 30
    }

    spawns {
        spawn(2884, 3430)
        spawn(2885, 3422)
        spawn(2885, 3435)
        spawn(2887, 3429)
        spawn(2888, 3440)
        spawn(2891, 3422)
        spawn(2893, 3433)
        spawn(2897, 3442)
    }
})
