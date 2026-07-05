package net.dodian.uber.game.npc

internal object Man : NpcFamily by npcFamily("Man", 1, block = {
    cache {
        examine = "A strange mole-like being. When on the wall: That white dot looks like an eye!"
    }

    server {
        defenceAnimation = 6013
        deathAnimation = 2304
        respawnTicks = 1
        hitpoints = 7
        attack = 40
        strength = 40
        defence = 50
        ranged = 1
    }

    spawns {
        spawn(2574, 9625)
    }
})
