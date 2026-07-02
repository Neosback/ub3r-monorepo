package net.dodian.uber.game.npc

internal object Zogre : NpcFamily by npcFamily("Zogre", 867, block = {
    server {
        deathAnimation = 2304
        hitpoints = 50
        attack = 20
        strength = 36
        defence = 35
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2465, 9444)
        spawn(2467, 9438)
        spawn(2467, 9442)
        spawn(2468, 9446)
        spawn(2470, 9443)
        spawn(2473, 9439)
        spawn(2475, 9445)
        spawn(2476, 9441)
    }
})
