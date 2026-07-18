package net.dodian.uber.game.engine.systems.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

data class CacheManifestFile(
    val name: String,
    val size: Long,
    val sha256: String,
)

data class CacheManifest(
    val schemaVersion: Int,
    val source: String? = null,
    val files: List<CacheManifestFile>,
)

object CacheManifestValidator {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val logger = org.slf4j.LoggerFactory.getLogger(CacheManifestValidator::class.java)

    /**
     * Validates [cachePath] against [manifestPath] if the manifest exists, otherwise logs a
     * warning and skips (a manifest is an opt-in, locally-provisioned pin, not a hard
     * requirement for every environment). When a manifest IS present, any mismatch is fatal —
     * this is what stops a substituted/corrupted cache directory from being served silently.
     */
    @JvmStatic
    @JvmOverloads
    fun validateIfPresent(
        cachePath: Path = Path.of("data/cache"),
        manifestPath: Path = Path.of("data/def/cache-manifest.json"),
    ): CacheManifest? {
        if (!Files.isRegularFile(manifestPath)) {
            logger.warn(
                "No cache manifest at {} — skipping cache integrity validation for {}. " +
                    "Generate one to catch a substituted/corrupted cache at startup instead of at runtime.",
                manifestPath.toAbsolutePath().normalize(),
                cachePath.toAbsolutePath().normalize(),
            )
            return null
        }
        val manifest = validateOrThrow(cachePath, manifestPath)
        logger.info("cache_manifest_verified files={} cachePath={}", manifest.files.size, cachePath.toAbsolutePath().normalize())
        return manifest
    }

    @JvmStatic
    @JvmOverloads
    fun validateOrThrow(
        cachePath: Path = Path.of("data/cache"),
        manifestPath: Path = Path.of("data/def/cache-manifest.json"),
    ): CacheManifest {
        require(Files.isRegularFile(manifestPath)) {
            "Missing server cache manifest: ${manifestPath.toAbsolutePath().normalize()}"
        }
        val manifest = Files.newBufferedReader(manifestPath).use { mapper.readValue(it, CacheManifest::class.java) }
        require(manifest.schemaVersion == 1) { "Unsupported cache manifest schema ${manifest.schemaVersion}" }
        require(manifest.files.isNotEmpty()) { "Cache manifest contains no files" }

        val duplicateNames = manifest.files.groupBy { it.name }.filterValues { it.size > 1 }.keys
        require(duplicateNames.isEmpty()) { "Duplicate cache manifest entries: ${duplicateNames.sorted()}" }

        for (entry in manifest.files) {
            require('/' !in entry.name && '\\' !in entry.name && entry.name.isNotBlank()) {
                "Invalid cache manifest filename: ${entry.name}"
            }
            val file = cachePath.resolve(entry.name)
            require(Files.isRegularFile(file)) { "Missing required Tarnish cache file: ${file.toAbsolutePath().normalize()}" }
            val actualSize = Files.size(file)
            require(actualSize == entry.size) {
                "Cache size mismatch for ${entry.name}: expected=${entry.size}, actual=$actualSize"
            }
            val actualHash = sha256(file)
            require(actualHash.equals(entry.sha256, ignoreCase = true)) {
                "Cache checksum mismatch for ${entry.name}: expected=${entry.sha256}, actual=$actualHash"
            }
        }
        return manifest
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}