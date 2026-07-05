package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.discord.DiscordService
import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.events.LevelUpEvent

object DiscordBotBootstrap {
    @JvmStatic
    fun bootstrap() {
        DiscordService.start()

        GameEventBus.on<LevelUpEvent> { event ->
            if (event.newLevel == 99) {
                val skillName = event.skill.getName().replaceFirstChar { it.uppercase() }
                DiscordService.sendToGeneral("${event.client.playerName} has just reached level 99 in $skillName!")
            }
            true
        }
    }
}
