package net.dodian.uber.game.engine.systems.pathing.collision

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

/**
 * Zone-indexed collision flag storage. Each 8x8 tile zone gets a lazily-allocated
 * [IntArray] of 64 ints. Only populated zones are retained: a direct reference
 * table for every possible world zone consumed about 64 MiB despite the live map
 * using only a few thousand zones.
 */
class CollisionMatrix {
    companion object {
        private const val ZONE_BITS = 3
        private const val ZONE_SIZE = 1 shl ZONE_BITS
        private const val ZONE_MASK = ZONE_SIZE - 1
        private const val TILES_PER_ZONE = ZONE_SIZE * ZONE_SIZE
        private const val ZONES_PER_AXIS = 2048
        private const val MAX_COORD = ZONES_PER_AXIS.shr(1) * ZONE_SIZE - 1
        private const val LOAD_FACTOR = 0.75f
        private const val REFERENCE_BYTES = 4L // compressed oops on the supported JVMs
        private const val MAP_ENTRY_BYTES = Int.SIZE_BYTES.toLong() + REFERENCE_BYTES
    }

    /** Game-thread-owned primitive zone key -> flat 8x8 collision flags. */
    private val zones = Int2ObjectOpenHashMap<IntArray>()

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
        val zone = zones.get(zi) ?: IntArray(TILES_PER_ZONE).also { zones.put(zi, it) }
        zone[ti] = zone[ti] or flags
    }

    fun clear(x: Int, y: Int, z: Int, flags: Int) {
        if (flags == 0 || !inBounds(x, y, z)) {
            return
        }
        val zi = zoneIndex(x, y, z)
        val ti = tileIndex(x, y)
        val zone = zones.get(zi) ?: return
        val updated = zone[ti] and flags.inv()
        zone[ti] = updated
        if (updated == 0 && zone.all { it == 0 }) {
            zones.remove(zi)
        }
    }

    fun getFlags(x: Int, y: Int, z: Int): Int {
        if (!inBounds(x, y, z)) {
            return 0
        }
        return zones.get(zoneIndex(x, y, z))?.get(tileIndex(x, y)) ?: 0
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
        zones.clear()
    }

    /** Count of allocated, non-empty 8x8 collision zones. */
    fun activeZoneCount(): Int = zones.size

    /** Exact bytes retained by zone payload arrays, excluding JVM array headers. */
    fun zonePayloadBytes(): Long = activeZoneCount().toLong() * TILES_PER_ZONE * Int.SIZE_BYTES

    /** Approximate primitive key/value table bytes for startup and heap diagnostics. */
    fun estimatedDirectoryBytes(): Long {
        val requiredSlots = (activeZoneCount() / LOAD_FACTOR).toInt() + 1
        var capacity = 2
        while (capacity < requiredSlots) capacity = capacity shl 1
        return capacity.toLong() * MAP_ENTRY_BYTES
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
