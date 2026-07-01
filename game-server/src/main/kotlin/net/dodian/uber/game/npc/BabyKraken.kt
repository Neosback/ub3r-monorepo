package net.dodian.uber.game.npc

internal object BabyKraken : NpcFamily by npcFamily("Baby kraken", 6640, block = {
    cache {
        examine = "Release the Kraken..eh baby?!"
    }

    spawns {
        spawn(2598, 3421)
    }
})
