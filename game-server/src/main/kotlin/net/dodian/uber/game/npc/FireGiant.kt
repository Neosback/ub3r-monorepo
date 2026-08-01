package net.dodian.uber.game.npc

internal object FireGiant : NpcFamily by npcFamily("Fire giant", 2075, block = {
    definition {
        examine = "A very large elemental adversary."
    }

    server {
        defenceAnimation = 4661
        attackAnimation = 4652
        deathAnimation = 4653
        respawnTicks = 40
        attack = 65
        strength = 80
        defence = 65
        hitpoints = 105
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2610, 9485)
        spawn(2611, 9483)
        spawn(2611, 9488)
        spawn(2614, 9488)
        spawn(2615, 9483)
        spawn(2616, 9486)
        spawn(3275, 9835)
    }
})
