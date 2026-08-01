package net.dodian.uber.game.npc

internal object Lizard : NpcFamily by npcFamily("Lizard", 458, block = {
    definition {
        examine = "how can it survive the heat?"
    }

    server {
        attackAnimation = 2776
        deathAnimation = 2778
        attack = 33
        strength = 33
        defence = 33
        hitpoints = 55
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(3412, 3060)
        spawn(3412, 3067)
        spawn(3415, 3062)
        spawn(3417, 3058)
        spawn(3418, 3066)
        spawn(3419, 3060)
        spawn(3422, 3064)
        spawn(3423, 3058)
        spawn(3427, 3060)
        spawn(3427, 3067)
        spawn(3428, 3064)
    }
})
