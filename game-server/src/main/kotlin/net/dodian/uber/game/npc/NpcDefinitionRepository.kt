package net.dodian.uber.game.npc

import java.nio.file.Path
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.engine.systems.cache.CacheStore
import net.dodian.uber.game.engine.systems.cache.NpcCacheDefinitionDecoder
import net.dodian.uber.game.engine.systems.cache.VarbitDefinitionDecoder
import org.slf4j.LoggerFactory

data class ResolvedNpcDefinition(
    val id: Int,
    val cache: CacheNpcDefinition,
    val server: NpcServerDefinition,
)

object NpcDefinitionRepository {
    private val logger = LoggerFactory.getLogger(NpcDefinitionRepository::class.java)

    fun load(cachePath: Path) =
        CacheStore(cachePath).open().use { store ->
            val definitions = NpcCacheDefinitionDecoder.decode(store).toMutableMap()
            val rawDefinitions = definitions.mapValues { (_, definition) -> definition.copy(actions = definition.actions.copyOf()) }
            val varbits = VarbitDefinitionDecoder.decode(store)
            NpcClientConfigService.initialize(varbits)
            NpcClientMorphService.initialize(rawDefinitions)
            for (override in ContentModuleIndex.npcModules.flatMap { it.definition.cacheOverrides }) {
                val definition = definitions[override.id] ?: continue
                override.applyTo(definition)
            }
            val validation = NpcClientOptionValidator.inspect(
                rawDefinitions = rawDefinitions,
                contents = ContentModuleIndex.npcContents,
                modules = ContentModuleIndex.npcModules,
                spawns = NpcSpawnRepository.all(),
                skillNpcBindings = ContentModuleIndex.skillPlugins.flatMap { it.definition.npcBindings },
                effectiveClientOverrides = NpcClientOptionValidator.loadEffectiveClientOverrides(rawDefinitions),
            )
            NpcClientOptionValidator.writeReports(
                rawDefinitions = rawDefinitions,
                resolvedDefinitions = definitions,
                contents = ContentModuleIndex.npcContents,
                modules = ContentModuleIndex.npcModules,
                spawns = NpcSpawnRepository.all(),
                validation = validation,
                varbits = varbits,
            )
            validation.warnings.forEach(logger::warn)
            if (validation.failures.isNotEmpty()) {
                error("NPC client option validation failed:\n${validation.failures.joinToString("\n")}")
            }
            val serverDefinitions = ContentModuleIndex.npcModules
                .flatMap { it.definition.serverDefinitions }
                .associateBy { it.id }
            definitions.mapValues { (id, cacheDefinition) ->
                ResolvedNpcDefinition(
                    id = id,
                    cache = cacheDefinition,
                    server = serverDefinitions[id] ?: NpcServerDefinition(id),
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
