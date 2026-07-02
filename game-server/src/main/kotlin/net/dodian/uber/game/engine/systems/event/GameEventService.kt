package net.dodian.uber.game.engine.systems.event

import java.util.EnumMap
import org.slf4j.LoggerFactory

object GameEventService {
    private val logger = LoggerFactory.getLogger(GameEventService::class.java)
    private val adminStates = EnumMap<ServerEvent, ActiveServerEvent>(ServerEvent::class.java)
    private val configStates = EnumMap<ServerEvent, ActiveServerEvent>(ServerEvent::class.java)
    private val schedulerStates = EnumMap<ServerEvent, ActiveServerEvent>(ServerEvent::class.java)
    private val systemStates = EnumMap<ServerEvent, ActiveServerEvent>(ServerEvent::class.java)

    @JvmStatic
    fun initialize() {
        logger.info("Global event service initialized: active={}", activeEvents().map { it.name })
    }

    @JvmStatic
    fun isActive(event: ServerEvent): Boolean = resolvedState(event)?.active == true

    @JvmStatic
    fun activeEvents(): Set<ServerEvent> =
        ServerEvent.values().filterTo(linkedSetOf()) { isActive(it) }

    @JvmStatic
    fun resolvedState(event: ServerEvent): ActiveServerEvent? =
        adminStates[event]
            ?: configStates[event]
            ?: schedulerStates[event]
            ?: systemStates[event]

    @JvmStatic
    fun setEvent(
        event: ServerEvent,
        active: Boolean,
        source: EventSource,
        reason: String? = null,
        endsAtMillis: Long? = null,
    ) {
        val state = ActiveServerEvent(
            event = event,
            active = active,
            source = source,
            reason = reason,
            endsAtMillis = endsAtMillis,
        )
        stateMap(source)[event] = state
        logger.info("Server event {} set active={} source={} reason={}", event, active, source, reason ?: "-")
    }

    @JvmStatic
    fun startEvent(event: ServerEvent, source: EventSource, reason: String? = null, endsAtMillis: Long? = null) {
        setEvent(event, active = true, source = source, reason = reason, endsAtMillis = endsAtMillis)
    }

    @JvmStatic
    fun stopEvent(event: ServerEvent, source: EventSource, reason: String? = null) {
        setEvent(event, active = false, source = source, reason = reason)
    }

    @JvmStatic
    fun setScheduledEvent(event: ServerEvent, active: Boolean, reason: String? = null, endsAtMillis: Long? = null) {
        if (active) {
            setEvent(event, active = true, source = EventSource.SCHEDULER, reason = reason, endsAtMillis = endsAtMillis)
        } else {
            stopScheduledEvent(event)
        }
    }

    @JvmStatic
    fun stopScheduledEvent(event: ServerEvent) {
        if (schedulerStates.remove(event) != null) {
            logger.info("Server event {} cleared scheduler state", event)
        }
    }

    fun clearForTests() {
        adminStates.clear()
        configStates.clear()
        schedulerStates.clear()
        systemStates.clear()
    }

    private fun stateMap(source: EventSource): EnumMap<ServerEvent, ActiveServerEvent> =
        when (source) {
            EventSource.ADMIN_COMMAND -> adminStates
            EventSource.CONFIG -> configStates
            EventSource.SCHEDULER -> schedulerStates
            EventSource.SYSTEM -> systemStates
        }
}
