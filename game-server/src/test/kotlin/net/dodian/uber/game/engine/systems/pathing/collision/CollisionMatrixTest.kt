package net.dodian.uber.game.engine.systems.pathing.collision

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollisionMatrixTest {
    @Test
    fun `missing and out of bounds zones are empty without allocation`() {
        val matrix = CollisionMatrix()

        assertEquals(0, matrix.getFlags(3200, 3200, 0))
        assertEquals(0, matrix.getFlags(-1, 3200, 0))
        assertEquals(0, matrix.activeZoneCount())
    }

    @Test
    fun `flags remain isolated by zone and plane`() {
        val matrix = CollisionMatrix()
        matrix.flag(3200, 3200, 0, CollisionFlag.BLOCKED)
        matrix.flag(3208, 3200, 1, CollisionFlag.IMPENETRABLE_BLOCKED)

        assertTrue(matrix.hasFlags(3200, 3200, 0, CollisionFlag.BLOCKED))
        assertFalse(matrix.hasFlags(3208, 3200, 0, CollisionFlag.BLOCKED))
        assertTrue(matrix.hasFlags(3208, 3200, 1, CollisionFlag.IMPENETRABLE_BLOCKED))
        assertEquals(2, matrix.activeZoneCount())
    }

    @Test
    fun `clearing final flag reclaims the sparse zone`() {
        val matrix = CollisionMatrix()
        matrix.flag(3200, 3200, 0, CollisionFlag.BLOCKED or CollisionFlag.IMPENETRABLE_BLOCKED)

        matrix.clear(3200, 3200, 0, CollisionFlag.BLOCKED)
        assertEquals(1, matrix.activeZoneCount())
        assertTrue(matrix.hasFlags(3200, 3200, 0, CollisionFlag.IMPENETRABLE_BLOCKED))

        matrix.clear(3200, 3200, 0, CollisionFlag.IMPENETRABLE_BLOCKED)
        assertEquals(0, matrix.activeZoneCount())
        assertEquals(0, matrix.getFlags(3200, 3200, 0))
    }

    @Test
    fun `clear all releases populated zones and metrics`() {
        val matrix = CollisionMatrix()
        repeat(3) { offset -> matrix.flag(3200 + offset * 8, 3200, 0, CollisionFlag.BLOCKED) }

        assertEquals(3, matrix.activeZoneCount())
        assertEquals(3L * 64L * Int.SIZE_BYTES, matrix.zonePayloadBytes())
        assertTrue(matrix.estimatedDirectoryBytes() > 0)

        matrix.clearAll()
        assertEquals(0, matrix.activeZoneCount())
        assertEquals(0L, matrix.zonePayloadBytes())
    }
}
