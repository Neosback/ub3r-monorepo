package net.dodian.uber.game.npc

internal object MenaphiteThug : NpcFamily by npcFamily("Menaphite thug", 3549, block = {
    definition {
        examine = "Just following orders"
    }

    server {
        attackAnimation = 401
        attack = 35
        strength = 35
        defence = 35
        hitpoints = 42
    }

    spawns {
        spawn(3340, 2962)
        spawn(3344, 2964)
        spawn(3345, 2951)
        spawn(3345, 2960)
        spawn(3346, 2956)
        spawn(3349, 2964)
        spawn(3350, 2958)
        spawn(3353, 2949)
        spawn(3353, 2955)
        spawn(3353, 2968)
        spawn(3356, 2952)
        spawn(3356, 2964)
        spawn(3358, 2958)
        spawn(3358, 2967)
        spawn(3360, 2955)
        spawn(3360, 2963)
        spawn(3362, 2967)
        spawn(3363, 2954)
        spawn(3364, 2963)
    }
})
