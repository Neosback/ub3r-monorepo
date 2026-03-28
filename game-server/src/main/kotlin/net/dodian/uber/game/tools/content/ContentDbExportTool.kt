package net.dodian.uber.game.tools.content

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import net.dodian.uber.game.persistence.db.DbTables
import net.dodian.uber.game.persistence.db.closeConnectionPool
import net.dodian.uber.game.persistence.db.dbConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

private data class ExportPayload(
    val source: String,
    val exportedAt: String,
    val table: String,
    val columns: List<String>,
    val rowCount: Int,
    val rows: List<Map<String, Any?>>,
)

object ContentDbExportTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val options = parseArgs(args)
        val outputDir = options["out"]?.let { Paths.get(it) }
            ?: Paths.get("src/main/resources/content/db-export")
        val includeTables = parseTableFilter(options["tables"])

        Files.createDirectories(outputDir)

        val preferred = listOf(
            DbTables.GAME_ITEM_DEFINITIONS,
            DbTables.GAME_NPC_DEFINITIONS,
            DbTables.GAME_OBJECT_DEFINITIONS,
            DbTables.GAME_DOOR_DEFINITIONS,
            DbTables.GAME_NPC_SPAWNS,
            DbTables.GAME_NPC_DROPS,
        )

        val selectedTables = if (includeTables.isEmpty()) preferred else preferred.filter { includeTables.contains(it.name.lowercase()) }
        require(selectedTables.isNotEmpty()) {
            "No export tables selected. Valid values: ${preferred.joinToString(",") { it.name.lowercase() }}"
        }

        val mapper = TomlMapper()

        dbConnection.use { connection ->
            selectedTables.forEach { table ->
                val snapshot = exportTable(connection, table)
                val outputFile = outputDir.resolve("${table.name.lowercase()}.toml")
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), snapshot)
                println("Exported ${snapshot.table} -> ${outputFile.toAbsolutePath()} (${snapshot.rowCount} rows)")
            }
        }

        closeConnectionPool()
        println("DB export complete. Output dir: ${outputDir.toAbsolutePath()}")
    }

    private fun exportTable(connection: java.sql.Connection, table: DbTables): ExportPayload {
        val tableName = table.toString()
        val columns = mutableListOf<String>()

        connection.prepareStatement("SELECT * FROM $tableName LIMIT 0").use { statement ->
            statement.executeQuery().use { rs ->
                val md = rs.metaData
                for (i in 1..md.columnCount) {
                    columns += md.getColumnLabel(i)
                }
            }
        }

        val orderColumns = columns.filter { it in preferredOrderColumns }
        val orderBy = if (orderColumns.isEmpty()) "" else " ORDER BY ${orderColumns.joinToString(",")}" 
        val sql = "SELECT * FROM $tableName$orderBy"

        val rows = ArrayList<Map<String, Any?>>()
        connection.prepareStatement(sql).use { statement ->
            statement.executeQuery().use { rs ->
                while (rs.next()) {
                    val row = LinkedHashMap<String, Any?>()
                    for (column in columns) {
                        row[column] = rs.getObject(column)
                    }
                    rows += row
                }
            }
        }

        return ExportPayload(
            source = tableName,
            exportedAt = Instant.now().toString(),
            table = table.name.lowercase(),
            columns = columns,
            rowCount = rows.size,
            rows = rows,
        )
    }

    private fun parseArgs(args: Array<String>): Map<String, String> {
        val parsed = LinkedHashMap<String, String>()
        var idx = 0
        while (idx < args.size) {
            val current = args[idx]
            if (current.startsWith("--") && idx + 1 < args.size) {
                parsed[current.removePrefix("--")] = args[idx + 1]
                idx += 2
            } else {
                idx++
            }
        }
        return parsed
    }

    private fun parseTableFilter(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) {
            return emptySet()
        }
        return raw.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }

    private val preferredOrderColumns = listOf(
        "id",
        "npc_id",
        "npcid",
        "item_id",
        "itemid",
        "object_id",
        "objectid",
        "x",
        "y",
        "height",
        "z",
    )
}
