package net.dodian.uber.game.npc

internal object LocustRiderRanged1 : NpcFamily by npcFamily("Locust rider ranged 1", 801, block = {
    definition {
        examine = "Ranged warrior of scarabs"
    }

    server {
        defenceAnimation = 5448
        attackAnimation = 5451
        deathAnimation = 5449
        respawnTicks = 30
        attack = 1
        strength = 80
        defence = 60
        hitpoints = 62
        ranged = 145
        magic = 300
    }

    spawns {
        spawn(3237, 2775)
        spawn(3308, 2829)
        spawn(3308, 2834)
        spawn(3310, 2831)
        spawn(3311, 2837)
        spawn(3312, 2828)
        spawn(3313, 2834)
        spawn(3315, 2831)
    }
})
