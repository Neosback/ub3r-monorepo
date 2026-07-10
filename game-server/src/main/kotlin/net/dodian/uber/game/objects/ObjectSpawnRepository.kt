package net.dodian.uber.game.objects

import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.objects.WorldObject
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.stream.Collectors

data class ObjectSpawnEntry(
    val id: Int,
    val name: String? = null,
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val type: Int = 10,
    val rotation: String? = null,
    val face: Int? = null,
    val sizeX: Int? = null,
    val sizeY: Int? = null,
    val solid: Boolean? = null,
    val walkable: Boolean? = null,
)

object ObjectSpawnRepository {
    @JvmStatic
    fun resolveSpawnsPath(): Path {
        val userDir = Path.of(System.getProperty("user.dir"))
        val base = if (userDir.fileName.toString() == "game-server") userDir else userDir.resolve("game-server")
        return base.resolve("src/main/kotlin/net/dodian/uber/game/objects/spawns")
    }

    @JvmStatic
    @JvmOverloads
    fun load(root: Path = resolveSpawnsPath()): List<WorldObject> {
        if (!Files.isDirectory(root)) {
            return emptyList()
        }

        val files = Files.walk(root).use { stream ->
            stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".toml") }
                .sorted()
                .collect(Collectors.toList())
        }

        val result = ArrayList<WorldObject>()
        for (file in files) {
            val content = Files.readString(file)
            val entries = parseToml(content)
            for (entry in entries) {
                val rotationFace = parseRotation(entry.rotation) ?: entry.face ?: 0
                val worldObj = WorldObject(entry.id, entry.x, entry.y, entry.z, entry.type, rotationFace)
                result.add(worldObj)

                if (entry.sizeX != null || entry.sizeY != null || entry.solid != null || entry.walkable != null) {
                    val defaultData = GameObjectData.forId(entry.id)
                    val solid = entry.solid ?: defaultData.isSolid()
                    val customData = GameObjectData(
                        id = entry.id,
                        name = entry.name ?: defaultData.name,
                        description = defaultData.description,
                        sizeX = entry.sizeX ?: defaultData.sizeX,
                        sizeY = entry.sizeY ?: defaultData.sizeY,
                        solid = solid,
                        impenetrable = if (entry.walkable == true) false else defaultData.isImpenetrable(),
                        hasActionsFlag = defaultData.hasActions(),
                        decoration = defaultData.isDecoration(),
                        walkType = 2,
                        blockWalk = if (solid) 2 else 0,
                        blockRange = solid,
                        breakRouteFinding = defaultData.breakRouteFinding(),
                        walkingFlag = defaultData.walkingFlag,
                        varbitId = defaultData.varbitId,
                        varpId = defaultData.varpId,
                        childIds = defaultData.childIds,
                    )
                    GameObjectData.addDefinition(customData)
                }
            }
        }
        return result
    }

    private fun parseToml(content: String): List<ObjectSpawnEntry> {
        val entries = mutableListOf<ObjectSpawnEntry>()
        var id = 0
        var name: String? = null
        var x = 0
        var y = 0
        var z = 0
        var type = 10
        var rotation: String? = null
        var face: Int? = null
        var sizeX: Int? = null
        var sizeY: Int? = null
        var solid: Boolean? = null
        var walkable: Boolean? = null
        var inObject = false
        var fieldsSeen = 0

        fun flush() {
            if (inObject && fieldsSeen >= 2) {
                entries += ObjectSpawnEntry(id, name, x, y, z, type, rotation, face, sizeX, sizeY, solid, walkable)
            }
            id = 0; name = null; x = 0; y = 0; z = 0; type = 10
            rotation = null; face = null; sizeX = null; sizeY = null; solid = null; walkable = null
            inObject = false; fieldsSeen = 0
        }

        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || trimmed.isEmpty()) continue

            if (trimmed == "[[object]]") {
                flush()
                inObject = true
                continue
            }

            if (inObject && trimmed.contains("=")) {
                val parts = trimmed.split("=", limit = 2)
                val key = parts[0].trim()
                val raw = parts[1].trim().trim('"')
                fieldsSeen++
                try {
                    when (key) {
                        "id" -> id = raw.toInt()
                        "name" -> name = raw
                        "x" -> x = raw.toInt()
                        "y" -> y = raw.toInt()
                        "z" -> z = raw.toInt()
                        "type" -> type = raw.toInt()
                        "rotation" -> rotation = raw
                        "face" -> face = raw.toIntOrNull()
                        "sizeX" -> sizeX = raw.toIntOrNull()
                        "sizeY" -> sizeY = raw.toIntOrNull()
                        "solid" -> solid = raw.lowercase(Locale.ROOT).toBooleanStrictOrNull()
                        "walkable" -> walkable = raw.lowercase(Locale.ROOT).toBooleanStrictOrNull()
                    }
                } catch (_: Exception) {
                }
            }
        }
        flush()
        return entries
    }

    private fun parseRotation(raw: String?): Int? {
        if (raw == null) return null
        return when (raw.uppercase(Locale.ROOT)) {
            "SOUTH", "0" -> 0
            "WEST", "1" -> 1
            "NORTH", "2" -> 2
            "EAST", "3" -> 3
            else -> raw.toIntOrNull()
        }
    }
}
