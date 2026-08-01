package net.dodian.uber.game.npc

internal object SandSnake : NpcFamily by npcFamily("Sand Snake", 7895, block = {
    server {
        hitpoints = 60
        attack = 30
        strength = 20
        defence = 20
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2393, 9875)
    }
})
