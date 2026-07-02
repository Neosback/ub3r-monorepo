package net.dodian.uber.game.npc

internal object CaptainRovin : NpcFamily by npcFamily("Captain Rovin", 884, block = {
    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(3243, 9893)
    }
})
