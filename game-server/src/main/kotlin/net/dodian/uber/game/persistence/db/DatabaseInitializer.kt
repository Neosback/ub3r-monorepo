package net.dodian.uber.game.persistence.db

import org.apache.ibatis.jdbc.ScriptRunner
import java.nio.file.Path

private val initializedFile = Path.of("./.initialized_database").toFile()

fun initializeDatabase() {
    if (initializedFile.exists()) return

    val dbSqlPath = Path.of("./").resolve("database")

    println("Initializing Dodian's database from the SQL files found at: ${dbSqlPath.toAbsolutePath()}")

    val startTime = System.currentTimeMillis() / 1000
    val scriptFiles = dbSqlPath.toFile().walk().filter { it.isFile }.sortedBy { it.name }.toList()
    dbConnection.use { conn ->
        for ((index, file) in scriptFiles.withIndex()) {
            val currentTime = System.currentTimeMillis() / 1000
            println("Importing file (${index + 1}/${scriptFiles.size}): ${file.absolutePath}")
            file.bufferedReader().use { reader ->
                val runner = ScriptRunner(conn).apply {
                    setAutoCommit(true)
                    setStopOnError(true)
                    setLogWriter(null)
                }
                runner.runScript(reader)
            }
            println("Took ${(System.currentTimeMillis() / 1000) - currentTime} seconds to import file: ${file.absolutePath}")
            println()
        }
    }
    println("Successfully imported database in ${(System.currentTimeMillis() / 1000) - startTime} seconds")
    initializedFile.createNewFile()
}

fun isDatabaseInitialized() = initializedFile.exists()
