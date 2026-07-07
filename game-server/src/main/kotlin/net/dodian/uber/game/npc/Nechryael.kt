package net.dodian.uber.game.npc

internal object Nechryael : NpcFamily by npcFamily("Nechryael", 8, block = {
    definition {
        examine = "An evil death demon."
    }

    server {
        defenceAnimation = 1529
        attackAnimation = 1528
        deathAnimation = 1530
        respawnTicks = 180
        attack = 140
        strength = 130
        defence = 140
        hitpoints = 350
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2698, 9773)
    }
})
