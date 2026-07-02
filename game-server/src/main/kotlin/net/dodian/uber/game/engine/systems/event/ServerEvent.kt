package net.dodian.uber.game.engine.systems.event

enum class ServerEvent {
    EASTER,
    HALLOWEEN,
    CHRISTMAS,
}

enum class EventSource {
    CONFIG,
    ADMIN_COMMAND,
    SCHEDULER,
    SYSTEM,
}

data class ActiveServerEvent(
    val event: ServerEvent,
    val active: Boolean,
    val source: EventSource,
    val reason: String? = null,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val endsAtMillis: Long? = null,
)
