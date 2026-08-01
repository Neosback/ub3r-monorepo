package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.engine.tasking.PlayerScopedCoroutineService
import net.dodian.uber.game.events.player.PlayerLogoutEvent


object PlayerScopedCoroutineLifecycleBootstrap {
    @JvmStatic
    fun bootstrap() {
        GameEventBus.on<PlayerLogoutEvent> { event ->
            PlayerScopedCoroutineService.cancelForPlayer(
                player = event.client,
                reason = "Player logout: ${event.reason}",
            )
            false
        }
    }
}