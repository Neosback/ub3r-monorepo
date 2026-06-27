package net.dodian.uber.game.npc

import java.nio.file.Path
import net.dodian.uber.game.engine.systems.cache.CacheStore
import net.dodian.uber.game.engine.systems.cache.NpcCacheDefinitionDecoder
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition

object NpcDefinitionRepository {
    fun load(cachePath: Path): Map<Int, CacheNpcDefinition> {
        val store = CacheStore(cachePath)
        return NpcCacheDefinitionDecoder.decode(store)
    }
}