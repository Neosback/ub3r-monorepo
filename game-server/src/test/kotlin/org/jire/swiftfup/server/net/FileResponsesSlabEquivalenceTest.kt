package org.jire.swiftfup.server.net

import com.displee.cache.CacheLibrary
import org.jire.swiftfup.server.FilePair
import org.jire.swiftfup.server.SwiftFupCache
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Regression for the slab consolidation in [FileResponses]: every served response must be
 * byte-identical to the archive data read straight from the cache, and every slice must
 * survive a full retain/release cycle (the Netty write lifecycle) without corrupting or
 * freeing the shared slab.
 */
class FileResponsesSlabEquivalenceTest {

    @Test
    fun `every slab response matches raw archive data and survives the netty write lifecycle`() {
        val cachePath = SwiftFupCache.path().toString()
        val responses = FileResponses()
        responses.load(cachePath, print = false)

        val library = CacheLibrary.create(cachePath)
        var verified = 0
        for (index in library.validIndices()) {
            for (archive in index.archives()) {
                val data = try {
                    index.readArchiveSector(archive.id)?.data
                } catch (exception: RuntimeException) {
                    null
                } ?: continue
                if (data.isEmpty()) continue

                val pair = FilePair(index.id, archive.id)
                val response = requireNotNull(responses[pair]) { "missing response for $pair" }

                // Simulate the Netty write path: retainedDuplicate, read, release.
                val retained = response.retainedDuplicate()
                try {
                    assertEquals(pair.bitpack, retained.readUnsignedMedium(), "bitpack mismatch for $pair")
                    assertEquals(data.size, retained.readInt(), "size mismatch for $pair")
                    val served = ByteArray(data.size)
                    retained.readBytes(served)
                    assertArrayEquals(data, served, "payload mismatch for $pair")
                } finally {
                    retained.release()
                }
                verified++
            }
        }
        library.close()

        // Second pass: after every slice was retained+released once, the slabs must still be
        // alive and intact (a refcount bug would have freed them).
        var reverified = 0
        val secondLibrary = CacheLibrary.create(cachePath)
        for (index in secondLibrary.validIndices()) {
            for (archive in index.archives()) {
                val data = try {
                    index.readArchiveSector(archive.id)?.data
                } catch (exception: RuntimeException) {
                    null
                } ?: continue
                if (data.isEmpty()) continue
                val pair = FilePair(index.id, archive.id)
                val dup = requireNotNull(responses[pair]).duplicate()
                dup.skipBytes(FilePair.SIZE_BYTES + Int.SIZE_BYTES)
                val served = ByteArray(data.size)
                dup.readBytes(served)
                assertArrayEquals(data, served, "payload corrupted after retain/release cycle for $pair")
                reverified++
            }
        }
        secondLibrary.close()

        assertEquals(verified, reverified)
        check(verified > 10_000) { "expected a full cache, only verified $verified archives" }
    }
}
