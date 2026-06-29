package net.dodian.uber.game.npc

import java.nio.file.Path
import net.dodian.uber.game.engine.systems.cache.CacheStore
import net.dodian.uber.game.engine.systems.cache.NpcCacheDefinitionDecoder
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.api.plugin.ContentModuleIndex

object NpcDefinitionRepository {
    fun load(cachePath: Path): Map<Int, CacheNpcDefinition> {
        val store = CacheStore(cachePath)
        val definitions = NpcCacheDefinitionDecoder.decode(store).toMutableMap()
        val overrides = NpcDefinitionOverrideRepository.load()
        for ((npcId, override) in overrides) {
            val definition = definitions[npcId]
                ?: error("NPC definition override references unknown cache npcId=$npcId")
            NpcDefinitionOverrideRepository.apply(definition, override)
        }
        for (module in ContentModuleIndex.npcModules) {
            val contentDef = module.definition
            for (override in contentDef.definitionOverrides) {
                val definition = definitions[override.id]
                    ?: error("Kotlin NPC definition override references unknown cache npcId=${override.id}")
                NpcDefinitionOverrideRepository.apply(definition, override)
            }
        }
        return definitions
    }
}
