package net.dodian.uber.game.npc

internal object Werewolf : NpcFamily by npcFamily("Werewolf", 2593, block = {
    cache {
        examine = "Big bad wolf"
    }

    runtime {
        attackAnimation = 6536
        deathAnimation = 6537
        attack = 72
        strength = 80
        defence = 70
        hitpoints = 105
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(3483, 3496)
        spawn(3484, 3491)
        spawn(3487, 3487)
        spawn(3487, 3494)
        spawn(3489, 3490)
        spawn(3491, 3493)
        spawn(3492, 3487)
        spawn(3494, 3490)
        spawn(3494, 3495)
        spawn(3496, 3498)
    }
})
