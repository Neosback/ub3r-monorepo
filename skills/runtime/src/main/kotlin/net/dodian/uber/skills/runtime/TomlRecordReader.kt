package net.dodian.uber.skills.runtime

/**
 * Minimal array-of-tables TOML reader for classpath-bundled skill data.
 *
 * Skill modules are compiled independently of :game-server and run merged onto
 * one classpath alongside every other skill module's resources, so data files
 * are loaded by resource path (not a filesystem-relative "content/" path like
 * :game-server's own bundled content) and callers should namespace their
 * resource path by skill (e.g. "cooking/recipes.toml") to avoid collisions.
 *
 * Values are returned as raw strings; callers convert fields with their own
 * defaults, matching the per-file typed parsers already used on the
 * :game-server side (see TomlProjectileLoader, TomlAncientSpellLoader).
 */
object TomlRecordReader {
    @JvmStatic
    @JvmOverloads
    fun readRecords(
        resource: String,
        table: String,
        classLoader: ClassLoader = TomlRecordReader::class.java.classLoader,
    ): List<Map<String, String>> {
        val stream = classLoader.getResourceAsStream(resource) ?: return emptyList()
        val marker = "[[$table]]"
        val records = mutableListOf<Map<String, String>>()
        var current: MutableMap<String, String>? = null

        stream.bufferedReader().useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("#") || trimmed.isEmpty()) continue

                if (trimmed.startsWith("[[") && trimmed.endsWith("]]")) {
                    current?.let { records += it.toMap() }
                    current = if (trimmed == marker) linkedMapOf() else null
                    continue
                }

                val record = current ?: continue
                if (!trimmed.contains("=")) continue
                val parts = trimmed.split("=", limit = 2)
                val key = parts[0].trim()
                val value = parts[1].trim().substringBefore("#").trim().trim('"')
                if (value.isEmpty()) continue
                record[key] = value
            }
        }
        current?.let { records += it.toMap() }
        return records
    }
}
