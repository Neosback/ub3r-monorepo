package net.dodian.uber.game.npc

internal object BloodBlamishSnail : NpcFamily by npcFamily("Blood Blamish Snail", 1228, block = {
    server {
        deathAnimation = 2304
        hitpoints = 10
        attack = 110
        strength = 100
        defence = 95
    }

    spawns {
        spawn(2962, 9633)
        spawn(2962, 9635)
    }
})
