package net.dodian.game.systems.action

sealed interface PlayerActionStopResult {
    object Completed : PlayerActionStopResult

    data class Cancelled(
        val reason: PlayerActionCancelReason,
    ) : PlayerActionStopResult
}
