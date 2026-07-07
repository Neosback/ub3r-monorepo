package net.dodian.uber.game.npc

internal object CaptainRovin : NpcFamily by npcFamily("Captain Rovin", 884, block = {
    definition {
        examine = "A sick, frail old man.  A sick, frail old man - transformed into an ogre."
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(3243, 9893)
    }
})
