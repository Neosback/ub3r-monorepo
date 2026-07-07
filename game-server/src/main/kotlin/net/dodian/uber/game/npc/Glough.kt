package net.dodian.uber.game.npc

internal object Glough : NpcFamily by npcFamily("Glough", 7102, block = {
    definition {
        examine = "A gnome consumed by his own wrath."
    }

    server {
        defenceAnimation = 4651
        hitpoints = 575
        attack = 260
        strength = 270
        defence = 248
        magic = 250
        ranged = 262
    }

    spawns {
        spawn(2392, 9903)
    }
})
