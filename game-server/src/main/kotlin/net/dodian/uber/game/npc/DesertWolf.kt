package net.dodian.uber.game.npc

internal object DesertWolf : NpcFamily by npcFamily("Desert wolf", 4649, block = {
    cache {
        examine = "People's worse enemy"
    }

    runtime {
        attackAnimation = 6559
        deathAnimation = 6558
        attack = 50
        strength = 60
        defence = 50
        hitpoints = 66
    }

    spawns {
        spawn(3314, 3032)
        spawn(3316, 3029)
        spawn(3316, 3035)
        spawn(3319, 3027)
        spawn(3319, 3038)
        spawn(3319, 3043)
        spawn(3321, 3030)
        spawn(3323, 3037)
        spawn(3324, 3042)
        spawn(3325, 3032)
        spawn(3327, 3027)
        spawn(3328, 3040)
        spawn(3329, 3035)
    }
})
