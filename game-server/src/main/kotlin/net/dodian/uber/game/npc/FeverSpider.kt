package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object FeverSpider : NpcScript("FeverSpider", 626) {
    override val definition = define {
        stats {
            attack = 60
            attackAnimation = 806
            deathAnimation = 2304
            defence = 40
            examine = "A bunch of legs, eyes and teeth."
            hitpoints = 40
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 30
        }

        spawns(
            spawn(3215, 9935, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3216, 9931, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3216, 9939, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3220, 9929, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3222, 9939, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3223, 9928, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3226, 9932, profile = profile("fever_spider.here_be_dead_stuff")),
            spawn(3226, 9936, profile = profile("fever_spider.here_be_dead_stuff")),
        )
    }
}
