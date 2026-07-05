package net.dodian.uber.game.npc

internal object SwordMummy : NpcFamily by npcFamily("Sword mummy", 953, block = {
    cache {
        examine = "What a long sworded mummy"
    }

    server {
        defenceAnimation = 5552
        attackAnimation = 5554
        deathAnimation = 5555
        respawnTicks = 50
        attack = 68
        strength = 85
        defence = 68
        hitpoints = 88
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3212, 2944)
        spawn(3214, 2940)
        spawn(3216, 2942)
        spawn(3217, 2938)
        spawn(3217, 2946)
        spawn(3219, 2940)
        spawn(3220, 2936)
        spawn(3221, 2942)
        spawn(3222, 2938)
        spawn(3223, 2945)
        spawn(3224, 2935)
        spawn(3225, 2940)
        spawn(3226, 2943)
        spawn(3228, 2938)
    }
})
