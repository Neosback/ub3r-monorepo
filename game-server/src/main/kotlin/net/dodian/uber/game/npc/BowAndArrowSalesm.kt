package net.dodian.uber.game.npc

internal object BowAndArrowSalesm : NpcFamily by npcFamily("Bow and Arrow salesm", 6060, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2589, 3083)
    }
})
