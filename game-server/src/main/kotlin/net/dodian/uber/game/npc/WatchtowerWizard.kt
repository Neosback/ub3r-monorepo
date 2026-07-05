package net.dodian.uber.game.npc

internal object WatchtowerWizard : NpcFamily by npcFamily("Watchtower wizard", 872, block = {
    cache {
        examine = "An undead skeletal ogre. It's falling apart! Looks like he's de-composing."
    }

    server {
        deathAnimation = 2304
        hitpoints = 71
        attack = 20
        strength = 36
        defence = 35
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2390, 9895)
        spawn(2393, 9899)
        spawn(2396, 9891)
    }
})
