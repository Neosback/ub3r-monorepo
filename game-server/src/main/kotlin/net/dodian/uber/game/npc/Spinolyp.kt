package net.dodian.uber.game.npc

internal object Spinolyp : NpcFamily by npcFamily("Spinolyp", 5961, block = {
    cache {
        examine = "no examine"
    }

    server {
        hitpoints = 100
        attack = 10
        strength = 10
        defence = 10
        magic = 1
        ranged = 100
    }

    spawns {
        spawn(2908, 4388)
    }
})
