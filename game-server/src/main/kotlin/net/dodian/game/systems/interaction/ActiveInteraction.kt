package net.dodian.game.systems.interaction

data class ActiveInteraction(
    val intent: InteractionIntent,
    val startedCycle: Long,
)
