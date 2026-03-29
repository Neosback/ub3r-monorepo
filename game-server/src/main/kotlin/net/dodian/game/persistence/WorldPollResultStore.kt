package net.dodian.game.persistence

import java.util.concurrent.atomic.AtomicReference
import net.dodian.game.persistence.world.WorldPollResult

object WorldPollResultStore {
    private val latest = AtomicReference(WorldPollResult.EMPTY)

    @JvmStatic
    fun latest(): WorldPollResult = latest.get()

    @JvmStatic
    fun publish(result: WorldPollResult?) {
        if (result != null) {
            latest.set(result)
        }
    }
}
