package org.jire.swiftfup.server

import java.nio.file.Files
import java.nio.file.Path

/**
 * Locates the cache shipped with the SwiftFUP server without depending on a
 * developer-specific absolute path. Both Gradle module runs and repository-root
 * runs are supported.
 */
object SwiftFupCache {
    private val relativePaths = listOf(
        Path.of("src", "main", "kotlin", "org", "jire", "swiftfup", "cache"),
        Path.of("game-server", "src", "main", "kotlin", "org", "jire", "swiftfup", "cache"),
    )

    @JvmStatic
    fun path(): Path = relativePaths
        .firstOrNull(Files::isDirectory)
        ?: error(
            "SwiftFUP cache directory was not found. Expected one of: " +
                relativePaths.joinToString(),
        )
}
