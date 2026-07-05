package net.dodian.uber.game.npc

internal object SigbertTheAdventur : NpcFamily by npcFamily("Sigbert the Adventur", 37, block = {
    server {
        defenceAnimation = 5568
        deathAnimation = 2304
        hitpoints = 22
        attack = 8
        strength = 9
        defence = 10
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3241, 9914)
    }
})
