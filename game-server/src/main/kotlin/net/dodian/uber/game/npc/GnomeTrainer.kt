package net.dodian.uber.game.npc

internal object GnomeTrainer : NpcFamily by npcFamily("Gnome trainer", 6080, block = {
    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(2474, 3439)
        spawn(2547, 3554)
        spawn(3002, 3931)
    }
})
