package net.dodian.uber.game.objects

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.dodian.cache.objects.GameObjectData
import net.dodian.uber.game.model.objects.WorldObject
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.stream.Collectors

data class ObjectSpawnJson(
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

data class ObjectSpawnFileJson(
    val schemaVersion: Int,
    val region: String,
    val objects: List<ObjectSpawnJson>,
)

object ObjectSpawnRepository {
    private val mapper = ObjectMapper().registerKotlinModule()

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
            stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
                .sorted()
                .collect(Collectors.toList())
        }

        val result = ArrayList<WorldObject>()
        for (file in files) {
            val fileJson = Files.newBufferedReader(file).use { mapper.readValue(it, ObjectSpawnFileJson::class.java) }
            require(fileJson.schemaVersion == 1) { "Unsupported object spawn schema version in $file: ${fileJson.schemaVersion}" }
            
            for (spawn in fileJson.objects) {
                val rotationFace = parseRotation(spawn.rotation) ?: spawn.face ?: 0
                val worldObj = WorldObject(spawn.id, spawn.x, spawn.y, spawn.z, spawn.type, rotationFace)
                result.add(worldObj)

                // If overrides are present, register them in GameObjectData
                if (spawn.sizeX != null || spawn.sizeY != null || spawn.solid != null || spawn.walkable != null) {
                    val defaultData = GameObjectData.forId(spawn.id)
                    val solid = spawn.solid ?: defaultData.isSolid()
                    val customData = GameObjectData(
                        id = spawn.id,
                        name = spawn.name ?: defaultData.name,
                        description = defaultData.description,
                        sizeX = spawn.sizeX ?: defaultData.sizeX,
                        sizeY = spawn.sizeY ?: defaultData.sizeY,
                        solid = solid,
                        impenetrable = if (spawn.walkable == true) false else defaultData.isImpenetrable(),
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
