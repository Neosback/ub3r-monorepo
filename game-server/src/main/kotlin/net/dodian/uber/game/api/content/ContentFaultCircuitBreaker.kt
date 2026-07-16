package net.dodian.uber.game.api.content

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import net.dodian.uber.game.engine.metrics.OperationalTelemetry
import org.slf4j.LoggerFactory

/**
 * Beta safety valve for content bindings. A repeatedly failing route is
 * quarantined without affecting other content or the game thread. State is
 * intentionally process-local and clears on restart.
 */
object ContentFaultCircuitBreaker {
    private const val FAILURE_THRESHOLD = 3
    private val logger = LoggerFactory.getLogger(ContentFaultCircuitBreaker::class.java)
    private val failures = ConcurrentHashMap<String, AtomicInteger>()
    private val disabled = ConcurrentHashMap.newKeySet<String>()

    @JvmStatic
    fun allows(bindingKey: String): Boolean = bindingKey !in disabled

    @JvmStatic
    fun recordFailure(bindingKey: String) {
        val count = failures.computeIfAbsent(bindingKey) { AtomicInteger() }.incrementAndGet()
        OperationalTelemetry.incrementCounter("content.fault")
        OperationalTelemetry.incrementCounter("content.fault.$bindingKey")
        if (count >= FAILURE_THRESHOLD && disabled.add(bindingKey)) {
            OperationalTelemetry.incrementCounter("content.quarantined")
            logger.error(
                "Content binding quarantined after {} failures binding={}; use the beta diagnostics/re-enable control after fixing it",
                count,
                bindingKey,
            )
        }
    }

    @JvmStatic
    fun reEnable(bindingKey: String): Boolean {
        failures.remove(bindingKey)
        return disabled.remove(bindingKey)
    }

    @JvmStatic
    fun snapshot(): Map<String, Any> = linkedMapOf(
        "failureThreshold" to FAILURE_THRESHOLD,
        "disabledBindings" to disabled.sorted(),
        "failureCounts" to failures.entries.sortedBy { it.key }.associate { it.key to it.value.get() },
    )

    internal fun resetForTests() {
        failures.clear()
        disabled.clear()
    }
}
