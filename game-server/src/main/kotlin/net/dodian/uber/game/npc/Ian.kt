package net.dodian.uber.game.npc

internal object Ian : NpcFamily by npcFamily("Ian", 1779, block = {
    cache {
        examine = "Who's your mummy?"
    }

    server {
        deathAnimation = 2304
    }

    spawns {
        spawn(1934, 4458, z = 2)
    }
})
