package net.dodian.uber.game.npc

internal object InfernalMage : NpcFamily by npcFamily("Infernal Mage", 443, block = {
    cache {
        examine = "An evil magic user."
    }

    server {
        deathAnimation = 2304
        attack = 60
        strength = 55
        defence = 45
        hitpoints = 60
        ranged = 60
        magic = 60
    }

    spawns {
        spawn(2716, 9743)
        spawn(2716, 9751)
        spawn(2718, 9747)
        spawn(2724, 9744)
    }
})
