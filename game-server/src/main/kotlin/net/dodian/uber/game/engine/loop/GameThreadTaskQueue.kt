package net.dodian.uber.game.engine.loop

object GameThreadTaskQueue {
    @JvmStatic
    fun submit(task: Runnable): Boolean {
        return submit("anonymous", task)
    }

    @JvmStatic
    fun submit(label: String, task: Runnable): Boolean {
        return GameThreadIngress.submitDeferred(label, task)
    }

    @JvmStatic
    fun drain() {
        drain(10_000)
    }

    @JvmStatic
    fun drain(maxTasks: Int) {
        GameThreadIngress.drainDeferred(maxTasks)
    }

    @JvmStatic
    fun clearForTests() {
        GameThreadIngress.clearForTests()
    }
}
