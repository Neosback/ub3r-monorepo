package net.dodian.uber.game.runtime.sync

import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import java.lang.management.ManagementFactory
import net.dodian.uber.game.Server
import net.dodian.uber.game.engine.sync.player.StagedPlayerSynchronizationService
import net.dodian.uber.game.engine.loop.GameThreadContext
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import net.dodian.uber.game.item.ItemManager
import net.dodian.uber.game.model.entity.player.Client

/** Manual throughput/allocation profiling tool for the staged player sync encoder. Not part of
 * the automated syncTest task — run directly when investigating SYNC_PLAYER_ENCODE performance. */
object SyncPipelineBenchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val iterations = args.firstOrNull()?.toIntOrNull()?.coerceAtLeast(100) ?: 2_000
        val previousItemManager = Server.itemManager
        GameThreadContext.bindCurrentThread()
        Server.itemManager = ItemManager(definitionLoader = { emptyMap() }, globalSpawnBootstrap = {})
        try {
            val staged = measure(iterations)
            println("mode,iterations,first_packet_us,packets_per_second,avg_packet_bytes,allocated_bytes")
            println(staged.csv())
        } finally {
            PlayerRegistry.players.fill(null)
            PlayerRegistry.playersOnline.clear()
            Server.itemManager = previousItemManager
        }
    }

    private fun measure(iterations: Int): Measurement {
        PlayerRegistry.players.fill(null)
        PlayerRegistry.playersOnline.clear()
        val clients = ArrayList<Client>(100)
        val viewer = client(1, 3200, 3200).also(clients::add)
        PlayerRegistry.players[1] = viewer
        for (slot in 2..100) {
            val subject = client(slot, 3200 + slot % 8, 3200 + slot % 7)
            clients += subject
            PlayerRegistry.players[slot] = subject
        }
        val service = StagedPlayerSynchronizationService()
        val firstStarted = System.nanoTime()
        service.synchronize(viewer)
        val firstMicros = (System.nanoTime() - firstStarted) / 1_000L
        drain(viewer)

        repeat(200) {
            service.synchronize(viewer)
            drain(viewer)
        }
        val bean = (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)
        val threadId = Thread.currentThread().id
        val allocatedBefore = bean?.takeIf { it.isThreadAllocatedMemorySupported }?.getThreadAllocatedBytes(threadId) ?: -1L
        var totalBytes = 0L
        val started = System.nanoTime()
        repeat(iterations) {
            service.synchronize(viewer)
            totalBytes += drain(viewer)
        }
        val elapsed = System.nanoTime() - started
        val allocatedAfter = bean?.takeIf { it.isThreadAllocatedMemorySupported }?.getThreadAllocatedBytes(threadId) ?: -1L
        clients.forEach {
            it.saveNeeded = false
            it.destruct()
        }
        return Measurement(
            "STAGED",
            iterations,
            firstMicros,
            iterations * 1_000_000_000.0 / elapsed,
            totalBytes.toDouble() / iterations,
            if (allocatedBefore >= 0L && allocatedAfter >= 0L) allocatedAfter - allocatedBefore else -1L,
        )
    }

    private fun client(slot: Int, x: Int, y: Int): Client =
        Client(EmbeddedChannel(), slot).apply {
            playerName = "bench$slot"
            moveTo(x, y, 0)
            loaded = true
            initialized = true
            isActive = true
            setSynchronizationReady(true)
        }

    private fun drain(client: Client): Long {
        val stats = client.flushOutbound()
        val channel = client.channel as EmbeddedChannel
        while (true) {
            val message = channel.readOutbound<Any>() ?: break
            ReferenceCountUtil.release(message)
        }
        return stats.flushedBytes().toLong()
    }

    private data class Measurement(
        val label: String,
        val iterations: Int,
        val firstPacketMicros: Long,
        val packetsPerSecond: Double,
        val averagePacketBytes: Double,
        val allocatedBytes: Long,
    ) {
        fun csv(): String =
            "$label,$iterations,$firstPacketMicros,${"%.1f".format(packetsPerSecond)},${"%.1f".format(averagePacketBytes)},$allocatedBytes"
    }
}
