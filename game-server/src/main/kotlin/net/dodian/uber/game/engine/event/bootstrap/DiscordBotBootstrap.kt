package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.discord.DiscordService
import net.dodian.uber.game.discord.DiscordAlert
import net.dodian.uber.game.discord.DiscordAlertKind
import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.events.LevelUpEvent
import net.dodian.uber.game.events.player.PlayerLoginEvent
import net.dodian.uber.game.events.player.PlayerLogoutEvent

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

        GameEventBus.on<PlayerLoginEvent> { event ->
            DiscordService.publishAlert(
                DiscordAlert(
                    kind = DiscordAlertKind.PLAYER_LOGIN,
                    title = "Player login",
                    detail = "${event.client.playerName} (id=${event.client.dbId}) logged in.",
                    deduplicationKey = "login:${event.client.dbId}:${event.client.currentGameCycle}",
                ),
            )
            false
        }
        GameEventBus.on<PlayerLogoutEvent> { event ->
            DiscordService.publishAlert(
                DiscordAlert(
                    kind = DiscordAlertKind.PLAYER_LOGOUT,
                    title = "Player logout",
                    detail = "${event.client.playerName} (id=${event.client.dbId}) logged out: ${event.reason}.",
                    deduplicationKey = "logout:${event.client.dbId}:${event.client.currentGameCycle}",
                ),
            )
            false
        }
    }
}
