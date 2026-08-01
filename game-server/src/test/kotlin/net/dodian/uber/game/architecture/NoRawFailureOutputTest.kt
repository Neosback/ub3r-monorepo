package net.dodian.uber.game.architecture

import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NoRawFailureOutputTest {
    @Test
    fun `server source does not emit raw stack traces or stderr`() {
        val offenders = buildList {
            Files.walk(Path.of("src/main")).use { paths ->
                paths.filter { it.toString().endsWith(".kt") || it.toString().endsWith(".java") }
                    .forEach { path ->
                        Files.readAllLines(path).forEachIndexed { index, raw ->
                            val line = raw.trim()
                            if (!line.startsWith("//") && !line.startsWith("*") &&
                                (line.contains("printStackTrace(") || line.contains("System.err.print"))
                            ) {
                                add("$path:${index + 1}")
                            }
                        }
                    }
            }
        }

        assertTrue(offenders.isEmpty(), "Raw failure output is forbidden: $offenders")
    }
}
