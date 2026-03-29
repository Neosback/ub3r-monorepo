package net.dodian.game.engine.phases

import net.dodian.game.Constants
import net.dodian.game.engine.sync.WorldSynchronizationService

open class OutboundPacketProcessor(
    private val syncRunner: (() -> Unit)? = null,
) : Runnable {
    override fun run() {
        runSynchronization()
    }

    protected open fun runSynchronization() {
        val runner = syncRunner
        if (runner != null) {
            runner()
            return
        }
        WorldSynchronizationService.INSTANCE.run()
    }
}
