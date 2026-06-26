package net.dodian.uber.game.engine.event.bootstrap

import net.dodian.uber.game.engine.event.GameEventBus
import net.dodian.uber.game.events.objects.ObjectExamineEvent


object ObjectExamineBootstrap {
    @JvmStatic
    fun bootstrap() {
        GameEventBus.on<ObjectExamineEvent> { event ->
            event.client.examineObject(event.client, event.objectId, event.position)
            true
        }
    }
}
