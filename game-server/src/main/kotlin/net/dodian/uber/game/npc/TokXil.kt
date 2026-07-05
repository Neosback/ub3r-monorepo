package net.dodian.uber.game.npc

internal object TokXil : NpcFamily by npcFamily("Tok-Xil", 2193, block = {
    cache {
        examine = "I don't like the look of those spines..."
    }

    server {
        defenceAnimation = 2634
        attackAnimation = 2633
        deathAnimation = 2630
        attack = 70
        strength = 100
        defence = 60
        hitpoints = 110
        ranged = 140
        magic = 60
    }

    spawns {
        spawn(2385, 5099)
        spawn(2386, 5092)
        spawn(2390, 5095)
        spawn(2392, 5098)
        spawn(2395, 5094)
        spawn(2400, 5099)
        spawn(2401, 5094)
        spawn(2406, 5103)
        spawn(2407, 5090)
        spawn(2411, 5096)
    }
})
