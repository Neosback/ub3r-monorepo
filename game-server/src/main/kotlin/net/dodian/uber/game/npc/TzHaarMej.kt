package net.dodian.uber.game.npc

internal object TzHaarMej : NpcFamily by npcFamily("TzHaar-Mej", 2154, block = {
    server {
        attackAnimation = 2612
        deathAnimation = 2607
        attack = 50
        strength = 60
        defence = 85
        hitpoints = 115
        magic = 1500
        ranged = 1
    }

    spawns {
        spawn(2451, 5151)
        spawn(2452, 5163)
        spawn(2455, 5154)
        spawn(2457, 5160)
        spawn(2461, 5157)
        spawn(2464, 5163)
        spawn(2466, 5168)
        spawn(2470, 5164)
        spawn(2470, 5169)
        spawn(2475, 5166)
        spawn(2480, 5167)
    }
})
