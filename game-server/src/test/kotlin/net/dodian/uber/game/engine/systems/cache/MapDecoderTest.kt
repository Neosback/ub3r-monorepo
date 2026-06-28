package net.dodian.uber.game.engine.systems.cache

import java.io.ByteArrayOutputStream
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MapDecoderTest {
    @Test
    fun `terrain decoder reads Tarnish short opcodes and attributes`() {
        val data = ByteArrayOutputStream()
        for (plane in 0 until 4) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    when {
                        plane == 0 && x == 10 && y == 10 -> {
                            data.writeShort(2)
                            data.writeShort(6)
                            data.writeShort(0)
                        }
                        plane == 0 && x == 11 && y == 10 -> {
                            data.writeShort(50)
                            data.writeShort(0)
                        }
                        plane == 1 && x == 12 && y == 10 -> {
                            data.writeShort(51)
                            data.writeShort(0)
                        }
                        else -> data.writeShort(0)
                    }
                }
            }
        }

        val grid =
            MapDecoder(CacheStore(Path.of("missing-cache-for-unit-test")))
                .decodeTileGrid(MapIndexEntry(regionId = 0, landscapeArchiveId = -1, objectArchiveId = -1), data.toByteArray())

        assertEquals(6, grid.getTile(10, 10, 0).overlay)
        assertFalse(grid.getTile(10, 10, 0).isBlocked())
        assertTrue(grid.getTile(11, 10, 0).isBlocked())
        assertTrue(grid.getTile(12, 10, 1).isBridge())
    }

    private fun ByteArrayOutputStream.writeShort(value: Int) {
        write((value ushr 8) and 0xFF)
        write(value and 0xFF)
    }
}
