package net.dodian.uber.game.npc

internal object Pyrefiend : NpcFamily by npcFamily("Pyrefiend", 433, block = {
    cache {
        examine = "A small fire demon."
    }

    server {
        defenceAnimation = 1581
        deathAnimation = 2304
        attack = 25
        strength = 15
        defence = 12
        hitpoints = 45
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2514, 3286)
        spawn(2518, 3285)
        spawn(2518, 3293)
        spawn(2520, 3275)
        spawn(2521, 3272)
        spawn(2521, 3290)
        spawn(2522, 3281)
        spawn(2524, 3293)
        spawn(2528, 3278)
        spawn(2530, 3281)
        spawn(2532, 3274)
        spawn(2535, 3287)
        spawn(2537, 3282)
        spawn(2538, 3278)
        spawn(2544, 3278)
        spawn(2545, 3281)
    }
})
