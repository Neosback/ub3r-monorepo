package net.dodian.game.engine.tasking.suspension

import kotlin.coroutines.Continuation

data class TaskStep(
    val condition: TaskCondition,
    val continuation: Continuation<Unit>,
)
