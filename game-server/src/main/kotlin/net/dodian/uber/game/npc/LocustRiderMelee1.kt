package net.dodian.uber.game.npc

internal object LocustRiderMelee1 : NpcFamily by npcFamily("Locust rider melee 1", 800, block = {
    definition {
        examine = "Melee warrior of scarabs"
    }

    server {
        defenceAnimation = 5448
        attackAnimation = 5450
        deathAnimation = 5449
        respawnTicks = 30
        attack = 50
        strength = 70
        defence = 60
        hitpoints = 70
        ranged = 70
        magic = 1
    }

    spawns {
        spawn(3241, 2775)
        spawn(3317, 2828)
        spawn(3319, 2825)
        spawn(3321, 2828)
        spawn(3322, 2823)
        spawn(3326, 2821)
        spawn(3326, 2827)
        spawn(3328, 2824)
    }
})
