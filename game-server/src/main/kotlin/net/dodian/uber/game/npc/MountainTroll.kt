package net.dodian.uber.game.npc

internal object MountainTroll : NpcFamily by npcFamily("Mountain Troll", 1107, block = {
    server {
        deathAnimation = 2304
        hitpoints = 91
    }

    spawns {
        spawn(3375, 2906)
    }
})
