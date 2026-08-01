package net.dodian.uber.game.npc

internal object BruiseBlamishSnail : NpcFamily by npcFamily("Bruise Blamish Snail", 1230, block = {
    definition {
        examine = "It is the avatar of the Arzinian Being of Bordanzan, representing ranging."
    }

    server {
        deathAnimation = 2304
        hitpoints = 13
    }

    spawns {
        spawn(2985, 9638)
        spawn(2987, 9640)
        spawn(2988, 9637)
        spawn(2989, 9634)
    }
})
