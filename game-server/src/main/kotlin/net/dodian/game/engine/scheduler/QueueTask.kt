package net.dodian.game.engine.scheduler

fun interface QueueTask {
    fun execute(): Boolean
}
