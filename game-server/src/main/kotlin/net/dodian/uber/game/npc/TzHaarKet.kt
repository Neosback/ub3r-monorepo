package net.dodian.uber.game.npc

internal object TzHaarKet : NpcFamily by npcFamily("TzHaar-Ket", 2173, block = {
    runtime {
        attackAnimation = 2609
        deathAnimation = 2608
        respawnTicks = 35
        attack = 100
        strength = 120
        defence = 100
        hitpoints = 155
        magic = 40
        ranged = 1
    }

    spawns {
        spawn(2883, 9768)
        spawn(2887, 9768)
        spawn(2890, 9770)
        spawn(2892, 9768)
        spawn(2893, 9772)
        spawn(2894, 9779)
        spawn(2895, 9769)
        spawn(2895, 9776)
        spawn(2897, 9766)
        spawn(2897, 9772)
        spawn(2900, 9764)
    }
})
