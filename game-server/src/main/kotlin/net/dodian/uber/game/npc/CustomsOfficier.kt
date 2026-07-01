package net.dodian.uber.game.npc

internal object CustomsOfficier : NpcFamily by npcFamily("customs officier", 3648, block = {
    spawns {
        spawn(2772, 3235, face = SOUTH)
        spawn(2804, 3421, face = WEST)
        spawn(2864, 2971, face = WEST)
        spawn(3274, 2797)
        spawn(3511, 3505)
    }
})
