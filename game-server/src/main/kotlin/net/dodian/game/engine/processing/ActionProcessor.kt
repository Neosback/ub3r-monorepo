package net.dodian.game.engine.processing

import net.dodian.game.engine.scheduler.QueueTaskService
import net.dodian.game.event.GameEventBus
import net.dodian.game.event.events.WorldTickEvent
import net.dodian.game.systems.world.player.PlayerRegistry

/**
 * Processes queued game actions/events in tick-order.
 */
class ActionProcessor : Runnable {
    override fun run() {
        QueueTaskService.processDue()
        GameEventBus.post(WorldTickEvent(PlayerRegistry.cycle))
    }
}
