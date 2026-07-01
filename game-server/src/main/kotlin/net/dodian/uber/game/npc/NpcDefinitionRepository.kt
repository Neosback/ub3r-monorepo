package net.dodian.uber.game.npc

import java.nio.file.Path
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.engine.systems.cache.CacheStore
import net.dodian.uber.game.engine.systems.cache.NpcCacheDefinitionDecoder

data class RuntimeNpcDefinition(
    val id: Int,
    val cache: CacheNpcDefinition,
    val runtime: NpcRuntimeDefinition,
)

object NpcDefinitionRepository {
    fun load(cachePath: Path) =
        CacheStore(cachePath).open().use { store ->
            val definitions = NpcCacheDefinitionDecoder.decode(store).toMutableMap()
            for (override in ContentModuleIndex.npcModules.flatMap { it.definition.cacheOverrides }) {
                val definition = definitions[override.id] ?: continue
                override.applyTo(definition)
            }
            val runtimeDefinitions = ContentModuleIndex.npcModules
                .flatMap { it.definition.runtimeDefinitions }
                .associateBy { it.id }
            definitions.mapValues { (id, cacheDefinition) ->
                RuntimeNpcDefinition(
                    id = id,
                    cache = cacheDefinition,
                    runtime = runtimeDefinitions[id] ?: NpcRuntimeDefinition(id),
                )
            }
        }
}

object NpcSpawnRepository {
    fun all(): List<NpcSpawnDef> =
        ContentModuleIndex.npcModules
            .filterIsInstance<NpcSpawnSource>()
            .flatMap { it.spawns }
}
