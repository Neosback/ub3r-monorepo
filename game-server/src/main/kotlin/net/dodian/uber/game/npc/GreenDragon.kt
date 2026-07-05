package net.dodian.uber.game.npc

internal object GreenDragon : NpcFamily by npcFamily("Green dragon", 260, block = {
    cache {
        examine = "Must be related to Elvarg."
    }

    server {
        attackAnimation = 91
        deathAnimation = 92
        respawnTicks = 40
        attack = 65
        strength = 70
        defence = 60
        hitpoints = 80
        magic = 68
        ranged = 1
    }

    spawns {
        spawn(3233, 9358)
        spawn(3235, 9353)
        spawn(3235, 9361)
        spawn(3238, 9350)
        spawn(3250, 9362)
        spawn(3250, 9371)
        spawn(3251, 9357)
        spawn(3251, 9367)
    }
})
