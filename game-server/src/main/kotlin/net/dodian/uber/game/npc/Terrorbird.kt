package net.dodian.uber.game.npc

internal object Terrorbird : NpcFamily by npcFamily("Terrorbird", 136, block = {
    definition {
        examine = "Big, ugly, and smelly. / A large dim looking humanoid."
    }

    server {
        deathAnimation = 2304
        hitpoints = 34
        attack = 43
        strength = 43
        defence = 43
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2461, 3049)
        spawn(2463, 3044)
        spawn(2466, 3053)
        spawn(2469, 3045)
        spawn(2469, 3050)
        spawn(2480, 3046)
        spawn(2486, 3048)
        spawn(2604, 9441)
        spawn(2605, 9436)
        spawn(2607, 9432)
        spawn(2609, 9440)
        spawn(2613, 9438)
    }
})
