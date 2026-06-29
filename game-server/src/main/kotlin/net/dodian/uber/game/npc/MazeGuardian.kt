package net.dodian.uber.game.npc

internal object MazeGuardian : NpcScript("MazeGuardian", 6777) {
    override val definition = define {
        stats {
            attack = 0
            defence = 0
            strength = 0
            ranged = 0
            magic = 0
            hitpoints = 0
            attackAnimation = 806
            deathAnimation = 836
            respawnTicks = 60
        }
    
        spawns(
        spawn(3257, 2791, profile = profile("jalsavrah_pyramid"))
        )
}
}
