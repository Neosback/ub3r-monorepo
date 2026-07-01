package net.dodian.uber.game.npc

internal object IceGiant : NpcFamily by npcFamily("Ice giant", 2085, block = {
    runtime {
        attackAnimation = 4672
        deathAnimation = 4653
        respawnTicks = 30
        attack = 45
        strength = 45
        defence = 35
        hitpoints = 70
        magic = 1
        ranged = 1
    }

    spawns {
        spawn(2717, 9843)
        spawn(2722, 9845)
        spawn(2725, 9844)
        spawn(2732, 9844)
        spawn(2863, 9951)
        spawn(2864, 9937)
        spawn(2867, 9934)
        spawn(2867, 9946)
        spawn(2868, 9940)
        spawn(2868, 9952)
        spawn(2870, 9949)
        spawn(2871, 9938)
        spawn(2874, 9945)
    }
})
