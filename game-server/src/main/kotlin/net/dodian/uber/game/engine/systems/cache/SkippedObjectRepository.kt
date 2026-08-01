package net.dodian.uber.game.engine.systems.cache

import java.nio.file.Files
import java.nio.file.Path

object SkippedObjectRepository {
    private val positionRegex = Regex(""""position"\s*:\s*\{\s*"x"\s*:\s*(-?\d+),\s*"y"\s*:\s*(-?\d+),\s*"z"\s*:\s*(-?\d+)\s*\}""")

    fun load(path: Path = Path.of("data/def/object/removed_objects.json")): Set<Long> {
        val text =
            if (Files.isRegularFile(path)) {
                Files.readString(path)
            } else {
                javaClass.classLoader
                    .getResource("def/object/removed_objects.json")
                    ?.readText()
                    ?: return emptySet()
            }
        return positionRegex
            .findAll(text)
            .map { match ->
                val x = match.groupValues[1].toInt()
                val y = match.groupValues[2].toInt()
                val z = match.groupValues[3].toInt()
                key(x, y, z)
            }
            .toSet()
    }

    fun key(x: Int, y: Int, z: Int): Long =
        ((z.toLong() and 0x3L) shl 32) or ((x.toLong() and 0xFFFFL) shl 16) or (y.toLong() and 0xFFFFL)
}
