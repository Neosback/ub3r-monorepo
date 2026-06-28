package net.dodian.uber.game.npc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition

@JsonIgnoreProperties(ignoreUnknown = true)
data class NpcDefinitionOverrideFile(
    val schemaVersion: Int = 1,
    val npcs: List<NpcDefinitionOverrideJson> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NpcDefinitionOverrideJson(
    val id: Int,
    val name: String? = null,
    val examine: String? = null,
    val size: Int? = null,
    val combatLevel: Int? = null,
    val standingAnimation: Int? = null,
    val walkingAnimation: Int? = null,
    val attackAnimation: Int? = null,
    val deathAnimation: Int? = null,
    val respawnTicks: Int? = null,
    val attack: Int? = null,
    val strength: Int? = null,
    val defence: Int? = null,
    val hitpoints: Int? = null,
    val ranged: Int? = null,
    val magic: Int? = null,
    val actions: List<String?>? = null,
)

object NpcDefinitionOverrideRepository {
    private val mapper = ObjectMapper().registerKotlinModule()
        .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS)
        .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_YAML_COMMENTS)
        .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_TRAILING_COMMA)

    @JvmStatic
    fun resolvePath(): Path {
        val userDir = Path.of(System.getProperty("user.dir"))
        val base = if (userDir.fileName.toString() == "game-server") userDir else userDir.resolve("game-server")
        return base.resolve("data/def/npc/overrides.jsonc")
    }

    @JvmStatic
    @JvmOverloads
    fun load(path: Path = resolvePath()): Map<Int, NpcDefinitionOverrideJson> {
        if (!Files.isRegularFile(path)) {
            return emptyMap()
        }
        val file = Files.newBufferedReader(path).use { mapper.readValue(it, NpcDefinitionOverrideFile::class.java) }
        require(file.schemaVersion == 1) { "Unsupported NPC definition override schema in $path: ${file.schemaVersion}" }
        val overrides = LinkedHashMap<Int, NpcDefinitionOverrideJson>()
        for (override in file.npcs) {
            require(override.id >= 0) { "Invalid NPC override id ${override.id} in $path" }
            require(overrides.putIfAbsent(override.id, override) == null) {
                "Duplicate NPC definition override id ${override.id} in $path"
            }
        }
        return overrides
    }

    fun apply(definition: CacheNpcDefinition, override: NpcDefinitionOverrideJson) {
        override.name?.let { definition.name = it }
        override.examine?.let { definition.examine = it }
        override.size?.let { definition.size = it }
        override.combatLevel?.let { definition.combatLevel = it }
        override.standingAnimation?.let { definition.standingAnimation = it }
        override.walkingAnimation?.let { definition.walkingAnimation = it }
        override.attackAnimation?.let { definition.attackAnimation = it }
        override.deathAnimation?.let { definition.deathAnimation = it }
        override.respawnTicks?.let { definition.respawnTicks = it }
        override.attack?.let { definition.attack = it }
        override.strength?.let { definition.strength = it }
        override.defence?.let { definition.defence = it }
        override.hitpoints?.let { definition.hitpoints = it }
        override.ranged?.let { definition.ranged = it }
        override.magic?.let { definition.magic = it }
        override.actions?.let { actions ->
            val next = arrayOfNulls<String>(5)
            for (index in 0 until minOf(5, actions.size)) {
                next[index] = actions[index]?.takeIf { it.isNotBlank() }
            }
            definition.actions = next
        }
    }
}
