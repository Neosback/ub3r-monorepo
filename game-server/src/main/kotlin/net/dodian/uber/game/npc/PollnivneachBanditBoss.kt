package net.dodian.uber.game.npc

internal object PollnivneachBanditBoss : NpcFamily by npcFamily("Pollnivneach bandit boss", 738, block = {
    cache {
        examine = "Believe he is the true Pollnivneach leader"
    }

    runtime {
        attackAnimation = 451
        deathAnimation = 2304
        respawnTicks = 155
        attack = 75
        strength = 110
        defence = 75
        hitpoints = 122
    }

    spawns {
        spawn(3365, 2992)
    }
})
