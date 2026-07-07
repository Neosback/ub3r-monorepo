package net.dodian.uber.game.npc

internal object GiantRat : NpcFamily by npcFamily("Giant rat", 2856, block = {
    definition {
        examine = "Overgrown vermin."
    }

    server {
        defenceAnimation = 4934
        attackAnimation = 4933
        deathAnimation = 4935
        respawnTicks = 20
        attack = 5
        strength = 8
        defence = 1
        hitpoints = 6
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2599, 3103, face = SOUTH, walkRadius = 2)
        spawn(2600, 3103, face = SOUTH, walkRadius = 2)
        spawn(2601, 3103, face = SOUTH, walkRadius = 2)
        spawn(2602, 3103, face = SOUTH, walkRadius = 2)
        spawn(3240, 9867, walkRadius = 2)
    }
})
