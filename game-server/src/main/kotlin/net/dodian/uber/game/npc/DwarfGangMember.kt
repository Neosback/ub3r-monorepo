package net.dodian.uber.game.npc

internal object DwarfGangMember : NpcFamily by npcFamily("Dwarf gang member", 1356, block = {
    definition {
        examine = "A short stout menacing fellow."
    }

    server {
        defenceAnimation = 100
        attackAnimation = 99
        deathAnimation = 102
        respawnTicks = 35
        attack = 33
        strength = 40
        defence = 32
        hitpoints = 50
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2555, 3087)
        spawn(2555, 3091)
        spawn(2556, 3089)
        spawn(2557, 3086)
        spawn(2558, 3092)
        spawn(2560, 3087)
        spawn(2560, 3090)
    }
})
