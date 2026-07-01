package net.dodian.uber.game.npc

internal object Cow : NpcFamily by npcFamily("Cow", 2790, block = {
    cache {
        name = "Cow"
        examine = "Meow meow I am a cow!"
    }

    runtime {
        attackAnimation = 5849
        deathAnimation = 5851
        respawnTicks = 30
        attack = 1
        strength = 1
        defence = 1
        hitpoints = 8
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2601, 3113)
        spawn(2602, 3116)
        spawn(2604, 3114)
        spawn(2605, 3116)
        spawn(2608, 3112)
        spawn(2609, 3115)
    }
})
