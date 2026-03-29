package net.dodian.game.engine.phases

import net.dodian.game.engine.processing.EntityProcessor

class InboundPacketPhase(private val entityProcessor: EntityProcessor) {
    fun run() {
        entityProcessor.runInboundPacketPhase()
    }
}
