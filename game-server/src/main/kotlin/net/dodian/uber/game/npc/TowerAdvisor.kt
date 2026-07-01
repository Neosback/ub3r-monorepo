package net.dodian.uber.game.npc

internal object TowerAdvisor : NpcFamily by npcFamily("Tower Advisor", 687, block = {
    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(3159, 2982)
    }
})
