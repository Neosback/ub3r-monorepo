package net.dodian.uber.game.npc

import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

internal object KnightOfArdougne : NpcScript("KnightOfArdougne", 3297) {
    override val definition = define {
        stats {
            attack = 38
            attackAnimation = 451
            deathAnimation = 2304
            defence = 31
            examine = "A member of Ardougne's militia."
            hitpoints = 52
            magic = 1
            ranged = 1
            respawnTicks = 60
            strength = 40
        }

        spawns(
            spawn(2658, 3306, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2659, 3310, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2660, 3301, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2662, 3309, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2663, 3304, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2664, 3311, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2665, 3301, profile = profile("knight_of_ardougne.ardougne")),
            spawn(2667, 3306, profile = profile("knight_of_ardougne.ardougne")),
        )
    }
}
