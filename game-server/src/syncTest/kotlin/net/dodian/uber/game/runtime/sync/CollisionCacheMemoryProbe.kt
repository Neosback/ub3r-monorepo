package net.dodian.uber.game.runtime.sync

import com.sun.management.HotSpotDiagnosticMXBean
import java.lang.management.ManagementFactory
import net.dodian.uber.game.engine.routing.WorldRouteService
import net.dodian.uber.game.engine.systems.cache.CacheBootstrapService

/** Loads the real cache collision state, enforces its memory estimate, and optionally dumps heap. */
object CollisionCacheMemoryProbe {
    private const val MAX_COLLISION_BYTES = 60L * 1024 * 1024

    @JvmStatic
    fun main(args: Array<String>) {
        val summary = CacheBootstrapService().bootstrap().summary
        val metrics = WorldRouteService.metrics()
        val estimatedBytes = metrics.estimatedRetainedBytes
        println(
            "Collision cache probe: regions=${summary.regionCount} tiles=${summary.tileCount} " +
                "objects=${summary.objectCount} openZones=${metrics.activeZones} " +
                "materializedZones=${metrics.materializedZones} pages=${metrics.activePages} " +
                "payloadBytes=${metrics.payloadBytes} directoryEstimate=${metrics.estimatedDirectoryBytes} " +
                "estimatedTotal=$estimatedBytes",
        )
        check(estimatedBytes <= MAX_COLLISION_BYTES) {
            "Collision storage estimate $estimatedBytes exceeds $MAX_COLLISION_BYTES bytes"
        }

        val heapDumpPath = args.firstOrNull() ?: return
        repeat(2) {
            System.gc()
            Thread.sleep(250)
        }
        val bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean::class.java)
        bean.dumpHeap(heapDumpPath, true)
        println("Collision cache heap dump: $heapDumpPath")
    }
}
