package net.dodian.uber.game.npc

internal object Guard2 : NpcFamily by npcFamily("Guard", 3094, block = {
    server {
        attackAnimation = 412
        deathAnimation = 2304
        respawnTicks = 30
        attack = 10
        strength = 22
        defence = 15
        hitpoints = 20
    }

    spawns {
        spawn(2615, 3103)
        spawn(2615, 3106)
        spawn(2617, 3104)
        spawn(2617, 3106)
        spawn(2619, 3104)
        spawn(2620, 3106)
    }
})
