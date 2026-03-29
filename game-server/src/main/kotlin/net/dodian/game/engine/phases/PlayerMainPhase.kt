package net.dodian.game.engine.phases

import net.dodian.game.engine.processing.EntityProcessor

class PlayerMainPhase(private val entityProcessor: EntityProcessor) {
    fun run() {
        entityProcessor.runPlayerMainPhase(System.currentTimeMillis())
    }
}
