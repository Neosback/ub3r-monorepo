package net.dodian.game.engine.scheduler

class QueueCancellationToken {
    @Volatile
    var cancelled: Boolean = false
        private set

    fun cancel() {
        cancelled = true
    }
}
