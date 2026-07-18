package net.dodian.uber.game.engine.systems.cache

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class CacheManifestValidatorTest {

    @Test
    fun `the served cache matches its known-good manifest`() {
        // Regression: a corrupted cache was once bundled and pointed at by SwiftFupCache without
        // anyone noticing, because nothing verified the served cache against a known-good
        // fingerprint. If this fails, `data/cache` no longer matches
        // `data/def/cache-manifest.json` and must not be served to clients as-is.
        val manifestPath = Path.of("data/def/cache-manifest.json")
        org.junit.jupiter.api.Assumptions.assumeTrue(
            Files.isRegularFile(manifestPath),
            "no local cache manifest at $manifestPath; skipping (see CacheManifestValidator KDoc)",
        )
        val manifest = CacheManifestValidator.validateOrThrow()
        assertTrue(manifest.files.isNotEmpty())
    }

    @Test
    fun `missing manifest is tolerated and returns null`(@TempDir tempDir: Path) {
        val result = CacheManifestValidator.validateIfPresent(
            cachePath = tempDir,
            manifestPath = tempDir.resolve("does-not-exist.json"),
        )
        assertNull(result)
    }

    @Test
    fun `mismatched cache file is rejected even when a manifest is present`(@TempDir tempDir: Path) {
        val cacheDir = Files.createDirectory(tempDir.resolve("cache"))
        val file = cacheDir.resolve("main_file_cache.dat")
        Files.write(file, byteArrayOf(1, 2, 3))

        val manifestPath = tempDir.resolve("cache-manifest.json")
        Files.writeString(
            manifestPath,
            """
            {
              "schemaVersion": 1,
              "files": [
                { "name": "main_file_cache.dat", "size": 3, "sha256": "0000000000000000000000000000000000000000000000000000000000000000000000000000" }
              ]
            }
            """.trimIndent(),
        )

        assertThrows(IllegalArgumentException::class.java) {
            CacheManifestValidator.validateOrThrow(cachePath = cacheDir, manifestPath = manifestPath)
        }
    }
}
