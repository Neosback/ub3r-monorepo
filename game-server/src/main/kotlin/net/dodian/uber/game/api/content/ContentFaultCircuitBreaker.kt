package net.dodian.uber.game.api.content

import java.util.concurrent.ConcurrentHashMap
import net.dodian.uber.game.engine.metrics.OperationalTelemetry
import org.slf4j.LoggerFactory

/**
 * Beta safety valve for content bindings. A repeatedly failing route is
 * quarantined without affecting other content or the game thread. State is
 * intentionally process-local and clears on restart.
 */
object ContentFaultCircuitBreaker {
    private const val FAILURE_THRESHOLD = 3
    private const val FAILURE_WINDOW_MS = 60_000L
    private const val QUARANTINE_COOLDOWN_MS = 300_000L
    private val logger = LoggerFactory.getLogger(ContentFaultCircuitBreaker::class.java)
    private data class FailureState(
        val timestamps: ArrayDeque<Long> = ArrayDeque(),
        var quarantinedUntilMs: Long = 0L,
        var totalFailures: Int = 0,
        var moduleId: String? = null,
        var lastFailureAtMs: Long = 0L,
    )
    private val failures = ConcurrentHashMap<String, FailureState>()

    @JvmStatic
    fun allows(bindingKey: String): Boolean {
        val state = failures[bindingKey] ?: return true
        synchronized(state) {
            if (state.quarantinedUntilMs == 0L) return true
            if (System.currentTimeMillis() < state.quarantinedUntilMs) return false
            state.quarantinedUntilMs = 0L
            state.timestamps.clear()
            logger.info("Content binding quarantine expired; retrying binding={}", bindingKey)
            return true
        }
    }

    @JvmStatic
    fun recordFailure(bindingKey: String, moduleId: String? = null) {
        val now = System.currentTimeMillis()
        val state = failures.computeIfAbsent(bindingKey) { FailureState() }
        val count = synchronized(state) {
            while (state.timestamps.firstOrNull()?.let { now - it > FAILURE_WINDOW_MS } == true) state.timestamps.removeFirst()
            state.timestamps.addLast(now)
            state.totalFailures++
            state.moduleId = moduleId ?: state.moduleId
            state.lastFailureAtMs = now
            if (state.timestamps.size >= FAILURE_THRESHOLD) state.quarantinedUntilMs = now + QUARANTINE_COOLDOWN_MS
            state.timestamps.size
        }
        OperationalTelemetry.incrementCounter("content.fault")
        OperationalTelemetry.incrementCounter("content.fault.$bindingKey")
        if (count >= FAILURE_THRESHOLD) {
            OperationalTelemetry.incrementCounter("content.quarantined")
            logger.error(
                "Content binding quarantined after {} failures in {}ms binding={} cooldownMs={}",
                count,
                FAILURE_WINDOW_MS,
                bindingKey,
                QUARANTINE_COOLDOWN_MS,
            )
        }
    }

    @JvmStatic
    fun reEnable(bindingKey: String): Boolean {
        val state = failures.remove(bindingKey) ?: return false
        return synchronized(state) { state.quarantinedUntilMs > 0L }
    }

    @JvmStatic
    fun snapshot(): Map<String, Any> = linkedMapOf(
        "failureThreshold" to FAILURE_THRESHOLD,
        "failureWindowMs" to FAILURE_WINDOW_MS,
        "quarantineCooldownMs" to QUARANTINE_COOLDOWN_MS,
        "disabledBindings" to failures.entries.sortedBy { it.key }.filter { (_, state) -> synchronized(state) { state.quarantinedUntilMs > System.currentTimeMillis() } }.map { it.key },
        "failureCounts" to failures.entries.sortedBy { it.key }.associate { (key, state) -> key to synchronized(state) { state.totalFailures } },
        "failureDetails" to failures.entries.sortedBy { it.key }.associate { (key, state) ->
            key to synchronized(state) {
                linkedMapOf(
                    "moduleId" to state.moduleId,
                    "recentFailures" to state.timestamps.size,
                    "totalFailures" to state.totalFailures,
                    "lastFailureAtMs" to state.lastFailureAtMs,
                    "quarantinedUntilMs" to state.quarantinedUntilMs,
                )
            }
        },
    )

    @JvmStatic
    fun failuresForModule(moduleId: String): Map<String, Map<String, Any?>> = failures.entries
        .filter { (_, state) -> synchronized(state) { state.moduleId == moduleId } }
        .sortedBy { it.key }
        .associate { (key, state) ->
            key to synchronized(state) {
                mapOf(
                    "recentFailures" to state.timestamps.size,
                    "totalFailures" to state.totalFailures,
                    "lastFailureAtMs" to state.lastFailureAtMs,
                    "quarantinedUntilMs" to state.quarantinedUntilMs,
                )
            }
        }

    internal fun resetForTests() {
        failures.clear()
    }
}
