package org.jire.swiftfup.server

import java.nio.file.Files
import java.nio.file.Path

/**
 * Locates the cache SwiftFUP serves to clients, without depending on a
 * developer-specific absolute path. Both Gradle module runs and repository-root
 * runs are supported.
 *
 * This must always resolve to the same cache [net.dodian.uber.game.engine.systems.cache.CacheBootstrapService]
 * decodes for server-side collision/objects (`data/cache`). Serving a different
 * cache than the one the server's own world state is built from causes client/server
 * object and clipping mismatches ("phantom" objects, blocked pathing) even when both
 * caches are individually well-formed. Do not point this at a separately bundled copy
 * (e.g. a snapshot of a client's local cache) — that reintroduces the same class of bug
 * and can propagate a single corrupted client cache to the whole fleet via checksum sync.
 */
object SwiftFupCache {
    private val relativePaths = listOf(
        Path.of("data", "cache"),
        Path.of("game-server", "data", "cache"),
    )

    @JvmStatic
    fun path(): Path = relativePaths
        .firstOrNull(Files::isDirectory)
        ?: error(
            "SwiftFUP cache directory was not found. Expected one of: " +
                relativePaths.joinToString(),
        )
}
