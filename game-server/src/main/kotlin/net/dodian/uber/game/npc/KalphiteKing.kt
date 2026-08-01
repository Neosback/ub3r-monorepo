package net.dodian.uber.game.npc

internal object KalphiteKing : NpcFamily by npcFamily("Kalphite king", 4304, block = {
    definition {
        examine = "King of all kalphites."
    }

    server {
        defenceAnimation = 6238
        attackAnimation = 6235
        deathAnimation = 6233
        respawnTicks = 250
        attack = 1
        strength = 66
        defence = 250
        hitpoints = 900
        ranged = 290
        magic = 1666
    }

    spawns {
        spawn(1711, 9845)
    }
})
