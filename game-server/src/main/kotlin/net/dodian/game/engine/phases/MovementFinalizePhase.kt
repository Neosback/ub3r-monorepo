package net.dodian.game.engine.phases

import net.dodian.game.engine.processing.EntityProcessor

class MovementFinalizePhase(private val entityProcessor: EntityProcessor) {
    fun run() {
        entityProcessor.runMovementFinalizePhase()
    }
}
