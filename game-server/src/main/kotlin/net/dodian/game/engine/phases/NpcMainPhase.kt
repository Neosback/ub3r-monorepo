package net.dodian.game.engine.phases

import net.dodian.game.engine.processing.EntityProcessor

class NpcMainPhase(private val entityProcessor: EntityProcessor) {
    fun run(now: Long) {
        entityProcessor.runNpcMainPhase(now)
    }
}
