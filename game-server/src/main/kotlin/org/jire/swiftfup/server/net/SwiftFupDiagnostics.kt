package org.jire.swiftfup.server.net

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder
import net.dodian.uber.game.engine.loop.GameThreadIngress
import net.dodian.uber.game.engine.metrics.OperationalTelemetry
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import org.jire.swiftfup.server.FilePair
import org.slf4j.LoggerFactory

/** Runtime diagnostics for the independent SwiftFUP cache connection. */
object SwiftFupDiagnostics {
    private const val MISS_LOG_WINDOW_MS = 30_000L
    private const val MAX_MISS_KEYS = 10_000
    private val logger = LoggerFactory.getLogger(SwiftFupDiagnostics::class.java)
    private val connections = LongAdder()
    private val activeConnections = AtomicInteger()
    private val handshakes = LongAdder()
    private val versionMismatches = LongAdder()
    private val requests = LongAdder()
    private val bytesServed = LongAdder()
    private val missingArchives = LongAdder()
    private val timeouts = LongAdder()
    private val channelFailures = LongAdder()
    private val misses = ConcurrentHashMap<MissKey, MissWindow>()

    fun connectionAccepted() {
        connections.increment()
        activeConnections.incrementAndGet()
        OperationalTelemetry.incrementCounter("swiftfup.connection.accepted")
    }

    fun connectionClosed() {
        activeConnections.updateAndGet { value -> (value - 1).coerceAtLeast(0) }
    }

    fun handshakeSucceeded() {
        handshakes.increment()
        OperationalTelemetry.incrementCounter("swiftfup.handshake.success")
    }

    fun versionMismatch() {
        versionMismatches.increment()
        OperationalTelemetry.incrementCounter("swiftfup.handshake.version_mismatch")
    }

    fun responseServed(bytes: Int) {
        requests.increment()
        bytesServed.add(bytes.coerceAtLeast(0).toLong())
        OperationalTelemetry.incrementCounter("swiftfup.request.total")
        OperationalTelemetry.incrementCounter("swiftfup.bytes.served", bytes.coerceAtLeast(0).toLong())
    }

    fun missingResponse(remote: String, pair: FilePair, presentAtStartup: Boolean) {
        requests.increment()
        missingArchives.increment()
        OperationalTelemetry.incrementCounter("swiftfup.request.total")
        OperationalTelemetry.incrementCounter("swiftfup.archive.missing")

        val now = System.currentTimeMillis()
        if (misses.size >= MAX_MISS_KEYS) {
            misses.entries.removeIf { now - it.value.lastLoggedAt >= MISS_LOG_WINDOW_MS * 2 }
        }
        val state = misses.computeIfAbsent(MissKey(remote, pair.bitpack)) { MissWindow() }
        val repeats: Int
        val total: Long
        synchronized(state) {
            state.total++
            if (state.lastLoggedAt != 0L && now - state.lastLoggedAt < MISS_LOG_WINDOW_MS) {
                state.suppressed++
                return
            }
            repeats = state.suppressed
            total = state.total
            state.suppressed = 0
            state.lastLoggedAt = now
        }
        logger.warn(
            "swiftfup_missing_archive remote={} index={} indexName={} archive={} startupCatalog={} " +
                "missCount={} suppressedRepeats={}",
            remote, pair.index, indexName(pair.index), pair.file, presentAtStartup, total, repeats,
        )
        enqueuePlayerContext(remote, pair, presentAtStartup, total, repeats)
    }

    fun timeout() {
        timeouts.increment()
        OperationalTelemetry.incrementCounter("swiftfup.channel.timeout")
    }

    fun channelFailure() {
        channelFailures.increment()
        OperationalTelemetry.incrementCounter("swiftfup.channel.failure")
    }

    @JvmStatic
    fun snapshot(): Map<String, Any> = linkedMapOf(
        "connections" to connections.sum(),
        "activeConnections" to activeConnections.get(),
        "handshakes" to handshakes.sum(),
        "versionMismatches" to versionMismatches.sum(),
        "requests" to requests.sum(),
        "bytesServed" to bytesServed.sum(),
        "missingArchives" to missingArchives.sum(),
        "timeouts" to timeouts.sum(),
        "channelFailures" to channelFailures.sum(),
        "trackedMisses" to misses.size,
    )

    internal fun resetForTests() {
        connections.reset()
        activeConnections.set(0)
        handshakes.reset()
        versionMismatches.reset()
        requests.reset()
        bytesServed.reset()
        missingArchives.reset()
        timeouts.reset()
        channelFailures.reset()
        misses.clear()
    }

    private fun enqueuePlayerContext(
        remote: String,
        pair: FilePair,
        presentAtStartup: Boolean,
        missCount: Long,
        repeats: Int,
    ) {
        val accepted = GameThreadIngress.submitDeferred("swiftfup-miss-diagnostic") {
            val matches = PlayerRegistry.playersOnline.values
                .asSequence()
                .filter { it.connectedFrom == remote && it.isActive && !it.disconnected }
                .sortedBy { it.slot }
                .toList()
            val players = when {
                matches.isEmpty() -> "unknown"
                matches.size == 1 -> matches.single().let {
                    "${it.playerName}#${it.slot}@(${it.position.x},${it.position.y},${it.position.z})"
                }
                else -> matches.take(4).joinToString(prefix = "ambiguous[", postfix = "] total=${matches.size}") {
                    "${it.playerName}#${it.slot}@(${it.position.x},${it.position.y},${it.position.z})"
                }
            }
            logger.info(
                "swiftfup_missing_archive_context remote={} index={} indexName={} archive={} startupCatalog={} " +
                    "missCount={} suppressedRepeats={} players={}",
                remote,
                pair.index,
                indexName(pair.index),
                pair.file,
                presentAtStartup,
                missCount,
                repeats,
                players,
            )
        }
        if (!accepted) OperationalTelemetry.incrementCounter("swiftfup.miss_context.rejected")
    }

    private fun indexName(index: Int): String = when (index) {
        0 -> "startup"
        1 -> "models"
        2 -> "animations"
        3 -> "music"
        4 -> "maps"
        31 -> "checksums"
        else -> "index-$index"
    }

    private data class MissKey(val remote: String, val bitpack: Int)
    private class MissWindow {
        var total = 0L
        var suppressed = 0
        @Volatile var lastLoggedAt = 0L
    }
}
