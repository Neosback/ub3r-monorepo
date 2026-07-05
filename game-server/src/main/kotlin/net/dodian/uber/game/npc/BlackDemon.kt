package net.dodian.uber.game.npc

internal object BlackDemon : NpcFamily by npcFamily("Black Demon", 1432, block = {
    cache {
        examine = "A big, scary, jet-black demon."
    }

    server {
        defenceAnimation = 65
        attackAnimation = 64
        deathAnimation = 67
        respawnTicks = 90
        attack = 185
        strength = 200
        defence = 185
        hitpoints = 360
        magic = 650
        ranged = 1
    }

    spawns {
        spawn(2910, 9804)
    }
})
