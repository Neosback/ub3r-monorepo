package net.dodian.uber.game.engine.systems.cache

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CacheCollisionAuditStoreTest {
    @Test
    fun `packed records round trip all retained fields`() {
        val cases =
            listOf(
                CacheCollisionAuditObject(0, 3200, 3200, 0, -1, 0, 0, 12850, "plane_underflow"),
                CacheCollisionAuditObject(50_046, 3263, 3263, 3, 3, 22, 3, 12850, "tarnish_removed_object"),
                CacheCollisionAuditObject(65_535, 3217, 3235, 2, 1, 10, 2, 12850, null),
            )

        for (expected in cases) {
            val actual = CacheCollisionAuditStore.unpack(expected.regionId, CacheCollisionAuditStore.pack(expected))
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `published region uses primitive accounting and lazy compatible view`() {
        val regionId = 12850
        val expected = CacheCollisionAuditObject(20_391, 3201, 3201, 1, 1, 10, 2, regionId)
        CacheCollisionAuditStore.publish(emptyList(), mapOf(regionId to longArrayOf(CacheCollisionAuditStore.pack(expected))))

        assertEquals(1L, CacheCollisionAuditStore.packedObjectCount())
        assertEquals(8L, CacheCollisionAuditStore.packedBytes())
        assertEquals(expected, CacheCollisionAuditStore.objectsForTile(3201, 3201).single())
        CacheCollisionAuditStore.clearForTests()
    }
}
