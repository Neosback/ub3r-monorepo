package net.dodian.uber.game.npc

internal object ScarabMage2 : NpcFamily by npcFamily("Scarab mage 2", 794, block = {
    definition {
        examine = "Magic warrior of scarabs"
    }

    server {
        attackAnimation = 414
        deathAnimation = 2304
        attack = 55
        strength = 85
        defence = 55
        hitpoints = 90
        magic = 1700
        ranged = 1
    }

    combat { handler(ScarabLocustCombat) }

    spawns {
        spawn(3246, 2779)
    }
})
