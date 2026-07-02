package net.dodian.uber.game.npc

internal object SlashBash : NpcFamily by npcFamily("Slash Bash", 882, block = {
    server {
        deathAnimation = 2304
        hitpoints = 100
        attack = 100
        strength = 120
        defence = 60
        magic = 1
        ranged = 100
    }

    spawns {
        spawn(2471, 9441)
        spawn(2475, 9444)
    }
})
