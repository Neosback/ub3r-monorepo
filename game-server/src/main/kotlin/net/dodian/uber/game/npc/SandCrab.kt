package net.dodian.uber.game.npc

internal object SandCrab : NpcFamily by npcFamily("Sand Crab", 5935, block = {
    cache {
        examine = "No one likes crabs... Disguised as sandy rocks:  A sandy outcrop."
    }

    server {
        attack = 1
        strength = 1
        defence = 1
        hitpoints = 45
        ranged = 1
        magic = 1
    }

    spawns {
        spawn(2789, 3222)
        spawn(3139, 2969)
        spawn(3142, 2968)
        spawn(3142, 2972)
        spawn(3144, 2992)
        spawn(3145, 2969)
        spawn(3145, 2988)
        spawn(3146, 2974)
        spawn(3146, 2984)
        spawn(3147, 2971)
        spawn(3148, 2989)
        spawn(3149, 2969)
        spawn(3149, 2976)
        spawn(3149, 2982)
        spawn(3149, 2986)
        spawn(3151, 2973)
        spawn(3151, 2980)
    }
})
