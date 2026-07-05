package net.dodian.uber.game.npc

internal object DagannothPrime : NpcFamily by npcFamily("Dagannoth Prime", 2266, block = {
    cache {
        examine = "A legendary Dagannoth King, rumoured to fly on the North winds."
    }

    server {
        attackAnimation = 2853
        deathAnimation = 2856
        respawnTicks = 120
        attack = 180
        strength = 180
        defence = 200
        hitpoints = 360
        ranged = 250
        magic = 255
    }

    spawns {
        spawn(2907, 9727)
    }
})
