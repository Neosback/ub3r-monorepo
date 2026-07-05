package net.dodian.uber.game.npc

internal object ChaosDruid : NpcFamily by npcFamily("Chaos druid", 520, block = {
    cache {
        examine = "Sells stuff."
        name = "Chaos druid"
    }

    server {
        deathAnimation = 2304
        respawnTicks = 25
        attack = 25
        strength = 20
        defence = 20
        hitpoints = 30
        magic = 10
        ranged = 1
    }

    spawns {
        spawn(2922, 3482)
        spawn(2923, 3485)
        spawn(2925, 3479)
        spawn(2926, 3487)
        spawn(2928, 3481)
        spawn(2928, 3485)
    }
})
