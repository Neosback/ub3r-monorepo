package net.dodian.uber.game.npc

internal object OchreBlamishSnail : NpcFamily by npcFamily("Ochre Blamish Snail", 1229, block = {
    runtime {
        deathAnimation = 2304
        hitpoints = 20
        attack = 70
        strength = 65
        defence = 50
    }

    spawns {
        spawn(2984, 9639)
        spawn(2985, 9632)
        spawn(2985, 9633)
        spawn(2985, 9635)
        spawn(2988, 9640)
    }
})
