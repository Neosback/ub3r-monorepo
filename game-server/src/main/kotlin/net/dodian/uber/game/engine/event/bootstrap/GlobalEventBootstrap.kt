package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.engine.systems.event.GameEventScheduleService
import net.dodian.uber.game.engine.systems.event.GameEventService
import net.dodian.uber.game.events.WorldTickEvent

object GlobalEventBootstrap {
    @JvmStatic
    fun bootstrap() {
        GameEventService.initialize()
        GameEventBus.on<WorldTickEvent> { event ->
            GameEventScheduleService.onWorldTick(event)
            false
        }
    }
}
