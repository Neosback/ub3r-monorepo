package net.dodian.uber.game.npc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.stream.Collectors
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition

data class NpcSpawnDefaultsJson(
    val facing: String? = null,
    val enabled: Boolean? = null,
    val walkRadius: Int? = null,
    val attackRange: Int? = null,
    val alwaysActive: Boolean? = null,
    val activity: String? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val defence: Int? = null,
    val strength: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
    val conditionKey: String? = null,
    val attackAnimation: Int? = null,
    val deathAnimation: Int? = null,
)

data class NpcSpawnJson(
    val x: Int,
    val y: Int,
    val plane: Int = 0,
    val facing: String? = null,
    val enabled: Boolean? = null,
    val walkRadius: Int? = null,
    val attackRange: Int? = null,
    val alwaysActive: Boolean? = null,
    val activity: String? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val defence: Int? = null,
    val strength: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
    val conditionKey: String? = null,
    val attackAnimation: Int? = null,
    val deathAnimation: Int? = null,
)

data class NpcSpawnGroupJson(
    val npcId: Int? = null,
    val name: String? = null,
    val defaults: NpcSpawnDefaultsJson = NpcSpawnDefaultsJson(),
    val spawns: List<NpcSpawnJson>,
)

data class NpcSpawnFamilyJson(
    val schemaVersion: Int,
    val family: String,
    val groups: List<NpcSpawnGroupJson>,
)

object NpcSpawnConditionRegistry {
    private val conditions = HashMap<String, (Client) -> Boolean>()

    @JvmStatic
    fun register(key: String, condition: (Client) -> Boolean) {
        require(key.isNotBlank()) { "NPC spawn condition key cannot be blank" }
        require(conditions.putIfAbsent(key, condition) == null) { "Duplicate NPC spawn condition key: $key" }
    }

    fun resolve(key: String?): (Client) -> Boolean {
        if (key.isNullOrBlank()) return { true }
        return conditions[key] ?: error("Unknown NPC spawn condition key: $key")
    }
}

object NpcSpawnRepository {
    private val mapper = ObjectMapper().registerKotlinModule()

    @JvmStatic
    fun resolveSpawnsPath(): Path {
        val userDir = Path.of(System.getProperty("user.dir"))
        val base = if (userDir.fileName.toString() == "game-server") userDir else userDir.resolve("game-server")
        return base.resolve("src/main/kotlin/net/dodian/uber/game/npc/spawns")
    }

    @JvmStatic
    @JvmOverloads
    fun load(
        root: Path = resolveSpawnsPath(),
        definitions: Map<Int, CacheNpcDefinition> = emptyMap(),
        definitionExists: (Int) -> Boolean = { true },
    ): List<NpcSpawnDef> {
        require(Files.isDirectory(root)) { "Missing NPC spawn directory: ${root.toAbsolutePath().normalize()}" }
        val files = Files.walk(root).use { stream ->
            stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".json") }
                .sorted()
                .collect(Collectors.toList())
        }
        require(files.isNotEmpty()) { "No NPC spawn JSON files found in ${root.toAbsolutePath().normalize()}" }

        val result = ArrayList<NpcSpawnDef>()
        val keys = HashSet<String>()
        for (file in files) {
            val family = Files.newBufferedReader(file).use { mapper.readValue(it, NpcSpawnFamilyJson::class.java) }
            require(family.schemaVersion == 1) { "Unsupported spawn schema in $file: ${family.schemaVersion}" }
            require(family.family.isNotBlank()) { "Blank spawn family in $file" }
            for (group in family.groups) {
                val npcId = group.npcId ?: group.name?.let { name ->
                    definitions.entries.firstOrNull {
                        it.value.name.equals(name, ignoreCase = true) ||
                        it.value.name.replace("_", " ").equals(name.replace("_", " "), ignoreCase = true)
                    }?.key
                } ?: error("Spawn group in $file must specify either npcId or name")

                require(npcId >= 0 && definitionExists(npcId)) {
                    "Unknown NPC definition $npcId (name=${group.name}) in $file"
                }
                require(group.spawns.isNotEmpty()) { "Empty NPC spawn group $npcId in $file" }
                for (spawn in group.spawns) {
                    require(spawn.x in 0..16383 && spawn.y in 0..16383) {
                        "Invalid NPC spawn coordinate id=$npcId (${spawn.x},${spawn.y}) in $file"
                    }
                    require(spawn.plane in 0..3) { "Invalid NPC spawn plane ${spawn.plane} in $file" }
                    val key = "$npcId:${spawn.x}:${spawn.y}:${spawn.plane}"
                    require(keys.add(key)) { "Duplicate NPC spawn key $key in $file" }

                    val facingName = spawn.facing ?: group.defaults.facing ?: "NORTH"
                    val facing = parseFacing(facingName)
                    val conditionKey = spawn.conditionKey ?: spawn.activity ?: group.defaults.conditionKey ?: group.defaults.activity
                    result += NpcSpawnDef(
                        npcId = npcId,
                        x = spawn.x,
                        y = spawn.y,
                        z = spawn.plane,
                        face = facing,
                        live = spawn.enabled ?: group.defaults.enabled ?: true,
                        walkRadius = spawn.walkRadius ?: group.defaults.walkRadius ?: 0,
                        attackRange = spawn.attackRange ?: group.defaults.attackRange ?: 6,
                        alwaysActive = spawn.alwaysActive ?: group.defaults.alwaysActive ?: false,
                        condition = NpcSpawnConditionRegistry.resolve(conditionKey),
                        respawnTicks = spawn.respawnTicks ?: group.defaults.respawnTicks ?: MYSQL_DEFAULT_STAT,
                        attack = spawn.attack ?: group.defaults.attack ?: MYSQL_DEFAULT_STAT,
                        defence = spawn.defence ?: group.defaults.defence ?: MYSQL_DEFAULT_STAT,
                        strength = spawn.strength ?: group.defaults.strength ?: MYSQL_DEFAULT_STAT,
                        hitpoints = spawn.hitpoints ?: group.defaults.hitpoints ?: MYSQL_DEFAULT_STAT,
                        ranged = spawn.ranged ?: group.defaults.ranged ?: MYSQL_DEFAULT_STAT,
                        magic = spawn.magic ?: group.defaults.magic ?: MYSQL_DEFAULT_STAT,
                        attackAnimation = spawn.attackAnimation ?: group.defaults.attackAnimation ?: MYSQL_DEFAULT_STAT,
                        deathAnimation = spawn.deathAnimation ?: group.defaults.deathAnimation ?: MYSQL_DEFAULT_STAT,
                    )
                }
            }
        }
        return result
    }

    private fun parseFacing(raw: String): Int =
        when (raw.uppercase(Locale.ROOT)) {
            "NONE" -> -1
            "NORTH" -> 0
            "NORTH_EAST" -> 1
            "EAST" -> 2
            "SOUTH_EAST" -> 3
            "SOUTH" -> 4
            "SOUTH_WEST" -> 5
            "WEST" -> 6
            "NORTH_WEST" -> 7
            else -> raw.toIntOrNull()?.takeIf { it in -1..7 } ?: error("Invalid NPC facing: $raw")
        }
}