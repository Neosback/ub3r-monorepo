package net.dodian.uber.game.npc

internal object Scorpion : NpcFamily by npcFamily("Scorpion", 2479, block = {
    cache {
        examine = "Has a nice sting to it"
    }

    runtime {
        attackAnimation = 6261
        deathAnimation = 6260
        attack = 55
        strength = 50
        defence = 60
        hitpoints = 55
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2524, 4776)
        spawn(3155, 2912)
        spawn(3158, 2909)
        spawn(3158, 2915)
        spawn(3159, 2902)
        spawn(3160, 2911)
        spawn(3161, 2905)
        spawn(3161, 2920)
        spawn(3162, 2917)
        spawn(3164, 2901)
        spawn(3164, 2922)
        spawn(3166, 2924)
        spawn(3167, 2898)
        spawn(3169, 2895)
        spawn(3172, 2927)
        spawn(3173, 2897)
        spawn(3176, 2895)
        spawn(3177, 2926)
        spawn(3178, 2898)
        spawn(3180, 2923)
        spawn(3182, 2900)
        spawn(3182, 2921)
        spawn(3183, 2905)
        spawn(3183, 2911)
        spawn(3184, 2918)
        spawn(3185, 2908)
        spawn(3185, 2915)
    }
})
