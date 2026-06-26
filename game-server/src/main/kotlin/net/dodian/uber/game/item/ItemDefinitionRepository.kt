package net.dodian.uber.game.item

import java.nio.file.Path
import net.dodian.uber.game.engine.systems.cache.CacheStore
import net.dodian.uber.game.engine.systems.cache.ItemCacheDefinitionDecoder
import net.dodian.uber.game.engine.systems.cache.CacheItemDefinition

object ItemDefinitionRepository {
    fun load(defPath: Path, cachePath: Path): Map<Int, CacheItemDefinition> {
        val store = CacheStore(cachePath)
        return ItemCacheDefinitionDecoder.decode(store)
    }
}