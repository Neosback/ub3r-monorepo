package net.dodian.uber.game.npc

internal object LocustRiderMelee2 : NpcFamily by npcFamily("Locust rider melee 2", 795, block = {
    cache {
        examine = "Melee warrior of scarabs"
    }

    server {
        attackAnimation = 5450
        deathAnimation = 5449
        attack = 70
        strength = 130
        defence = 90
        hitpoints = 110
        ranged = 100
        magic = 1
    }

    spawns {
        spawn(3241, 2779)
    }
})
