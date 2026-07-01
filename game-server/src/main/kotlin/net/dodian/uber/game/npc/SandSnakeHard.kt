package net.dodian.uber.game.npc

internal object SandSnakeHard : NpcFamily by npcFamily("Sand Snake (hard)", 7894, block = {
    runtime {
        hitpoints = 180
        attack = 180
        strength = 140
        defence = 20
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2391, 9877)
        spawn(2442, 9881)
        spawn(2445, 9892)
    }
})
