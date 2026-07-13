package net.dodian.uber.game.npc

internal object ScarabMage1 : NpcFamily by npcFamily("Scarab mage 1", 799, block = {
    definition {
        examine = "Magic warrior of scarabs"
    }

    server {
        attackAnimation = 414
        deathAnimation = 2304
        respawnTicks = 30
        attack = 35
        strength = 55
        defence = 35
        hitpoints = 58
        magic = 1350
        ranged = 1
    }

    combat { handler(ScarabLocustCombat) }

    spawns {
        spawn(3246, 2775)
        spawn(3307, 2821)
        spawn(3308, 2824)
        spawn(3310, 2818)
        spawn(3310, 2821)
        spawn(3311, 2824)
        spawn(3313, 2819)
        spawn(3314, 2823)
        spawn(3316, 2821)
    }
})
