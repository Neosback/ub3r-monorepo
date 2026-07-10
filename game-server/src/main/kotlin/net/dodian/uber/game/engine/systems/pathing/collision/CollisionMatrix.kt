package net.dodian.uber.game.engine.systems.pathing.collision

/**
 * Zone-indexed collision flag storage. Each 8x8 tile zone gets a lazily-allocated
 * [IntArray] of 64 ints, indexed by pure arithmetic with no hashing and no wrapper
 * objects. This replaces the previous [HashMap]&lt;Long, Int&gt; which degraded to
 * millions of tree-node objects due to a poor hash distribution.
 */
class CollisionMatrix {
    companion object {
        private const val ZONE_BITS = 3
        private const val ZONE_SIZE = 1 shl ZONE_BITS
        private const val ZONE_MASK = ZONE_SIZE - 1
        private const val TILES_PER_ZONE = ZONE_SIZE * ZONE_SIZE
        private const val ZONES_PER_PLANE = 2048 * 2048
        private const val TOTAL_ZONES = ZONES_PER_PLANE * 4
        private const val MAX_COORD = ZONES_PER_PLANE.shr(1) * ZONE_SIZE - 1
    }

    private val zones = arrayOfNulls<IntArray>(TOTAL_ZONES)

    fun apply(update: CollisionUpdate) {
        for (dx in 0 until update.width) {
            for (dy in 0 until update.height) {
                if (update.remove) {
                    clear(update.x + dx, update.y + dy, update.z, update.flags)
                } else {
                    flag(update.x + dx, update.y + dy, update.z, update.flags)
                }
            }
        }
    }

    fun flag(x: Int, y: Int, z: Int, flags: Int) {
        if (flags == 0 || !inBounds(x, y, z)) {
            return
        }
        val zi = zoneIndex(x, y, z)
        val ti = tileIndex(x, y)
        val zone = zones[zi] ?: IntArray(TILES_PER_ZONE).also { zones[zi] = it }
        zone[ti] = zone[ti] or flags
    }

    fun clear(x: Int, y: Int, z: Int, flags: Int) {
        if (flags == 0 || !inBounds(x, y, z)) {
            return
        }
        val zi = zoneIndex(x, y, z)
        val ti = tileIndex(x, y)
        val zone = zones[zi] ?: return
        val updated = zone[ti] and flags.inv()
        zone[ti] = updated
    }

    fun getFlags(x: Int, y: Int, z: Int): Int {
        if (!inBounds(x, y, z)) {
            return 0
        }
        return zones[zoneIndex(x, y, z)]?.get(tileIndex(x, y)) ?: 0
    }

    fun hasFlags(x: Int, y: Int, z: Int, flags: Int): Boolean {
        return getFlags(x, y, z) and flags != 0
    }

    fun hasAllFlags(x: Int, y: Int, z: Int, flags: Int): Boolean {
        if (flags == 0) {
            return true
        }
        return getFlags(x, y, z) and flags == flags
    }

    fun clearAll() {
        for (i in zones.indices) {
            zones[i] = null
        }
    }

    private fun zoneIndex(x: Int, y: Int, z: Int): Int =
        ((x shr ZONE_BITS) and 0x7FF) or
            (((y shr ZONE_BITS) and 0x7FF) shl 11) or
            ((z and 0x3) shl 22)

    private fun tileIndex(x: Int, y: Int): Int =
        (x and ZONE_MASK) or ((y and ZONE_MASK) shl ZONE_BITS)

    private fun inBounds(x: Int, y: Int, z: Int): Boolean =
        x in 0..MAX_COORD && y in 0..MAX_COORD && z in 0..3
}
