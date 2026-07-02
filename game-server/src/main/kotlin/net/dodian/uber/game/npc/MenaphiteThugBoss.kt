package net.dodian.uber.game.npc

internal object MenaphiteThugBoss : NpcFamily by npcFamily("Menaphite thug boss", 3551, block = {
    cache {
        examine = "Come from a far land to be the true Pollnivneach leader"
    }

    server {
        attackAnimation = 401
        respawnTicks = 155
        attack = 75
        strength = 110
        defence = 75
        hitpoints = 122
        magic = 80
    }

    spawns {
        spawn(3350, 2949)
    }
})
