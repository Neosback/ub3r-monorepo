package net.dodian.uber.game.npc

internal object MyreBlamishSnail : NpcFamily by npcFamily("Myre Blamish Snail", 1227, block = {
    definition {
        examine = "It is the avatar of the Arzinian Being of Bordanzan, representing strength."
    }

    server {
        deathAnimation = 2304
        hitpoints = 8
    }

    spawns {
        spawn(2960, 9632)
        spawn(2962, 9633)
        spawn(2963, 9632)
        spawn(2963, 9635)
        spawn(2964, 9636)
    }
})
