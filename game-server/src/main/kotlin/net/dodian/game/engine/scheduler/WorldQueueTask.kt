package net.dodian.game.engine.scheduler

class WorldQueueTask(
    private val delegate: QueueTask,
) : QueueTask {
    override fun execute(): Boolean = delegate.execute()
}
