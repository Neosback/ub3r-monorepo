package net.dodian.uber.game.npc

internal object Wolf : NpcFamily by npcFamily("Wolf", 106, block = {
    cache {
        examine = "Not man's best friend."
    }

    server {
        defenceAnimation = 6559
        attackAnimation = 6559
        deathAnimation = 6558
        attack = 30
        strength = 30
        defence = 30
        hitpoints = 70
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2686, 9803)
        spawn(2688, 9805)
        spawn(2689, 9809)
        spawn(2694, 9824)
        spawn(2695, 9820)
        spawn(2695, 9830)
        spawn(2839, 3503)
        spawn(2840, 3466)
        spawn(2842, 3490)
        spawn(2843, 3464)
        spawn(2846, 3459)
        spawn(2846, 3490)
        spawn(2846, 3497)
        spawn(2848, 3478)
    }
})
