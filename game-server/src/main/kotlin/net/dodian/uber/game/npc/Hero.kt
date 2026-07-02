package net.dodian.uber.game.npc

internal object Hero : NpcFamily by npcFamily("Hero", 3295, block = {
    cache {
        name = "Hero"
    }

    server {
        attackAnimation = 451
        deathAnimation = 2304
        respawnTicks = 35
        attack = 45
        strength = 45
        defence = 40
        hitpoints = 83
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(2539, 3091)
        spawn(2539, 3095)
        spawn(2539, 3098)
        spawn(2540, 3089, face = EAST)
        spawn(2541, 3097)
        spawn(2542, 3090, face = EAST)
        spawn(2543, 3088)
    }
})
