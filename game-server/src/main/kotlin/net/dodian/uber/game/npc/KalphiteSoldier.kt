package net.dodian.uber.game.npc

internal object KalphiteSoldier : NpcFamily by npcFamily("Kalphite Soldier", 1154, block = {
    runtime {
        attackAnimation = 6224
        deathAnimation = 6228
        respawnTicks = 50
        attack = 65
        strength = 65
        defence = 50
        hitpoints = 70
    }

    spawns {
        spawn(3462, 9481, z = 2)
        spawn(3464, 9501, z = 2)
        spawn(3464, 9511, z = 2)
        spawn(3465, 9488, z = 2)
        spawn(3466, 9506, z = 2)
        spawn(3467, 9478, z = 2)
        spawn(3469, 9514, z = 2)
        spawn(3470, 9501, z = 2)
        spawn(3471, 9488, z = 2)
        spawn(3473, 9522, z = 2)
        spawn(3474, 9517, z = 2)
        spawn(3475, 9485, z = 2)
        spawn(3475, 9525, z = 2)
        spawn(3478, 9483, z = 2)
        spawn(3478, 9501, z = 2)
        spawn(3479, 9487, z = 2)
        spawn(3481, 9491, z = 2)
        spawn(3481, 9525, z = 2)
        spawn(3483, 9502, z = 2)
        spawn(3485, 9490, z = 2)
        spawn(3490, 9501, z = 2)
        spawn(3491, 9490, z = 2)
        spawn(3495, 9498, z = 2)
        spawn(3496, 9493, z = 2)
    }
})
