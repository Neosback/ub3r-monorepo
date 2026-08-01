package net.dodian.uber.game.api.plugin

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory

/** Plugin-safe boundaries around the authoritative game tick. */
enum class ContentTickPhase { PRE_SIMULATION, POST_SIMULATION, POST_OUTBOUND }

object ContentTickPhases {
    private data class Registration(val owner: String, val action: () -> Unit)
    private val handlers = ConcurrentHashMap<ContentTickPhase, MutableList<Registration>>()
    private val current = ThreadLocal<ContentTickPhase?>()
    private val logger = LoggerFactory.getLogger(ContentTickPhases::class.java)

    fun register(owner: String, phase: ContentTickPhase, action: () -> Unit): AutoCloseable {
        require(owner.isNotBlank()) { "Phase handler owner cannot be blank" }
        val registration = Registration(owner, action)
        val bucket = handlers.computeIfAbsent(phase) { mutableListOf() }
        synchronized(bucket) {
            require(bucket.none { it.owner == owner }) { "Duplicate phase handler owner=$owner phase=$phase" }
            bucket += registration
            bucket.sortBy { it.owner }
        }
        val closed = AtomicBoolean(false)
        return AutoCloseable { if (closed.compareAndSet(false, true)) synchronized(bucket) { bucket.remove(registration) } }
    }

    fun run(phase: ContentTickPhase) {
        check(current.get() == null) { "Nested content tick phases are not supported" }
        current.set(phase)
        try {
            val bucket = handlers[phase] ?: return
            synchronized(bucket) { bucket.toList() }.forEach { registration ->
                try { registration.action() } catch (error: Throwable) {
                    logger.error("Content phase handler failed owner={} phase={}", registration.owner, phase, error)
                }
            }
        } finally { current.remove() }
    }

    fun currentPhase(): ContentTickPhase? = current.get()
    fun clearForTests() = handlers.clear()
}
