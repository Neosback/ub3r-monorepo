package net.dodian.uber.game.npc

internal object TzhaarMejJal : NpcFamily by npcFamily("Tzhaar-Mej-Jal", 2180, block = {
    definition {
        examine = "Another one of those mystic-types."
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2849, 2991)
    }
})
