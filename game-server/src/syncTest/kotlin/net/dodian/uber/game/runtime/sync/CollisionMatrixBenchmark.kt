package net.dodian.uber.game.runtime.sync

import net.dodian.uber.game.engine.systems.pathing.collision.CollisionFlag
import net.dodian.uber.game.engine.systems.pathing.collision.CollisionMatrix

/** Lightweight repeatable lookup benchmark; run manually, never as a test gate. */
object CollisionMatrixBenchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val matrix = CollisionMatrix()
        val zones = args.firstOrNull()?.toIntOrNull() ?: 3_478
        repeat(zones) { index ->
            val x = 3200 + (index and 0x3f) * 8
            val y = 3200 + ((index ushr 6) and 0x3f) * 8
            matrix.flag(x, y, index and 0x3, CollisionFlag.BLOCKED)
        }

        var checksum = 0
        val started = System.nanoTime()
        repeat(1_000_000) { index ->
            val x = 3200 + (index and 0x3f) * 8
            val y = 3200 + ((index ushr 6) and 0x3f) * 8
            checksum = checksum xor matrix.getFlags(x, y, index and 0x3)
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000.0
        println(
            "Collision matrix benchmark: zones=${matrix.activeZoneCount()} payloadBytes=${matrix.zonePayloadBytes()} " +
                "directoryEstimate=${matrix.estimatedDirectoryBytes()} lookups=1000000 elapsedMs=$elapsedMs checksum=$checksum",
        )
    }
}
