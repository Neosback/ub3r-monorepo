package net.dodian.uber.game.npc

internal object Jatix : NpcFamily by npcFamily("Jatix", 8532, block = {
    cache {
        name = "Jatix"
        examine = "He knows about herblore"
    }

    runtime {
        deathAnimation = 2304
    }

    spawns {
        spawn(2897, 3428)
    }
})
