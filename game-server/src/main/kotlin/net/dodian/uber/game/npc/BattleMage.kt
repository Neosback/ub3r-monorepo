package net.dodian.uber.game.npc

internal object BattleMage : NpcFamily by npcFamily("Battle mage", 1611, block = {
    definition {
        examine = "Kills in the name of Saradomin/Zamorak/Guthix."
    }

    server {
        attackAnimation = 414
        respawnTicks = 30
        attack = 60
        strength = 45
        defence = 80
        hitpoints = 75
        ranged = 50
        magic = 3500
    }

    spawns {
        spawn(2624, 3083)
        spawn(2625, 3080)
        spawn(2626, 3078)
        spawn(2628, 3084)
        spawn(2629, 3078)
    }
})
