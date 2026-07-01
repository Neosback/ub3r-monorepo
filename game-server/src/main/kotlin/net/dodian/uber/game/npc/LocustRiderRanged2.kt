package net.dodian.uber.game.npc

internal object LocustRiderRanged2 : NpcFamily by npcFamily("Locust rider ranged 2", 796, block = {
    cache {
        examine = "Ranged warrior of scarabs"
    }

    runtime {
        attackAnimation = 5451
        deathAnimation = 5449
        attack = 20
        strength = 110
        defence = 80
        hitpoints = 92
        ranged = 210
        magic = 300
    }

    spawns {
        spawn(3237, 2780)
    }
})
