package net.dodian.uber.game.runtime.sync

import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.flag.CollisionFlag

/** Manual dense-versus-paged collision lookup and mutation benchmark. */
object CollisionMatrixBenchmark {
    private const val LOOKUPS = 5_000_000
    private const val MUTATIONS = 500_000

    @JvmStatic
    fun main(args: Array<String>) {
        val regions = args.firstOrNull()?.toIntOrNull() ?: 2_607
        require(regions in 1..65_536)

        val sparse = CollisionFlagMap()
        val dense = DenseCollisionMap()
        repeat(regions) { region ->
            val baseX = (region and 0xFF) shl 6
            val baseZ = ((region ushr 8) and 0xFF) shl 6
            repeat(4) { level ->
                sparse.markRegionPlaneAllocated(baseX, baseZ, level)
                dense.markRegionPlaneAllocated(baseX, baseZ, level)
                repeat(64) { zone ->
                    if ((region + level + zone) % 3 == 0) {
                        val x = baseX + ((zone and 0x7) shl 3)
                        val z = baseZ + ((zone ushr 3) shl 3)
                        sparse.add(x, z, level, CollisionFlag.BLOCK_WALK)
                        dense.add(x, z, level, CollisionFlag.BLOCK_WALK)
                    }
                }
            }
        }

        repeat(2) {
            benchmarkLookups(sparse::get, regions, LOOKUPS / 5)
            benchmarkLookups(dense::get, regions, LOOKUPS / 5)
        }
        val sparseLookup = benchmarkLookups(sparse::get, regions, LOOKUPS)
        val denseLookup = benchmarkLookups(dense::get, regions, LOOKUPS)
        val sparseMutation = benchmarkMutations(sparse, regions)
        val denseMutation = benchmarkMutations(dense, regions)
        val metrics = sparse.metrics()

        println(
            "Collision map benchmark: regions=$regions openZones=${metrics.openZones} " +
                "materializedZones=${metrics.materializedZones} pages=${metrics.activePages} " +
                "payloadBytes=${metrics.payloadBytes} directoryEstimate=${metrics.estimatedDirectoryBytes} " +
                "retainedEstimate=${metrics.estimatedRetainedBytes}",
        )
        println(
            "lookupNsPerOp sparse=${sparseLookup.nsPerOp} dense=${denseLookup.nsPerOp} " +
                "ratio=${"%.3f".format(sparseLookup.nsPerOp / denseLookup.nsPerOp)} " +
                "checksum=${sparseLookup.checksum xor denseLookup.checksum}",
        )
        println(
            "mutationNsPerOp sparse=${sparseMutation.nsPerOp} dense=${denseMutation.nsPerOp} " +
                "ratio=${"%.3f".format(sparseMutation.nsPerOp / denseMutation.nsPerOp)}",
        )
    }

    private fun benchmarkLookups(get: (Int, Int, Int) -> Int, regions: Int, iterations: Int): Result {
        var checksum = 0
        val started = System.nanoTime()
        repeat(iterations) { index ->
            val region = index % regions
            val x = ((region and 0xFF) shl 6) + ((index ushr 5) and 0x3F)
            val z = (((region ushr 8) and 0xFF) shl 6) + ((index ushr 11) and 0x3F)
            checksum = checksum xor get(x, z, index and 0x3)
        }
        return Result((System.nanoTime() - started).toDouble() / iterations, checksum)
    }

    private fun benchmarkMutations(map: CollisionFlagMap, regions: Int): Result {
        val started = System.nanoTime()
        repeat(MUTATIONS) { index ->
            val region = index % regions
            val x = ((region and 0xFF) shl 6) + 63
            val z = (((region ushr 8) and 0xFF) shl 6) + 63
            val level = index and 0x3
            map.add(x, z, level, CollisionFlag.LOC_ROUTE_BLOCKER)
            map.remove(x, z, level, CollisionFlag.LOC_ROUTE_BLOCKER)
        }
        return Result((System.nanoTime() - started).toDouble() / (MUTATIONS * 2), 0)
    }

    private fun benchmarkMutations(map: DenseCollisionMap, regions: Int): Result {
        val started = System.nanoTime()
        repeat(MUTATIONS) { index ->
            val region = index % regions
            val x = ((region and 0xFF) shl 6) + 63
            val z = (((region ushr 8) and 0xFF) shl 6) + 63
            val level = index and 0x3
            map.add(x, z, level, CollisionFlag.LOC_ROUTE_BLOCKER)
            map.remove(x, z, level, CollisionFlag.LOC_ROUTE_BLOCKER)
        }
        return Result((System.nanoTime() - started).toDouble() / (MUTATIONS * 2), 0)
    }

    private data class Result(val nsPerOp: Double, val checksum: Int)

    private class DenseCollisionMap {
        private val flags = arrayOfNulls<IntArray>(2048 * 2048 * 4)

        operator fun get(x: Int, z: Int, level: Int): Int =
            flags[zoneIndex(x, z, level)]?.get(tileIndex(x, z)) ?: -1

        fun add(x: Int, z: Int, level: Int, mask: Int) {
            val zone = zoneIndex(x, z, level)
            val payload = flags[zone] ?: IntArray(64).also { flags[zone] = it }
            val tile = tileIndex(x, z)
            payload[tile] = payload[tile] or mask
        }

        fun remove(x: Int, z: Int, level: Int, mask: Int) {
            val payload = flags[zoneIndex(x, z, level)] ?: return
            val tile = tileIndex(x, z)
            payload[tile] = payload[tile] and mask.inv()
        }

        fun markRegionPlaneAllocated(x: Int, z: Int, level: Int) {
            repeat(8) { zoneX ->
                repeat(8) { zoneZ ->
                    val index = zoneIndex(x + (zoneX shl 3), z + (zoneZ shl 3), level)
                    if (flags[index] == null) flags[index] = IntArray(64)
                }
            }
        }

        private fun tileIndex(x: Int, z: Int): Int = (x and 0x7) or ((z and 0x7) shl 3)

        private fun zoneIndex(x: Int, z: Int, level: Int): Int =
            ((x shr 3) and 0x7FF) or (((z shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)
    }
}
