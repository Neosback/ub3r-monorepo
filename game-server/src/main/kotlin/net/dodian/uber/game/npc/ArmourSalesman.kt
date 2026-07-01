package net.dodian.uber.game.npc

internal object ArmourSalesman : NpcFamily by npcFamily("Armour salesman", 6059, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2725, 3369)
    }
})
