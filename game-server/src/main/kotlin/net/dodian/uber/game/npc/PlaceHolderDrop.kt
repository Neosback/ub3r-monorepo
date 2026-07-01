package net.dodian.uber.game.npc

internal object PlaceHolderDrop : NpcFamily by npcFamily("Place Holder Drop", 1337, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(1811, 4495)
    }
})
