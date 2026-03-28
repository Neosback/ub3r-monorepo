package net.dodian.uber.game.systems.world.cache

import net.dodian.uber.game.config.mysticCacheDir
import net.dodian.uber.game.world.cache.Cache
import net.dodian.uber.game.world.cache.`object`.GameObjectData
import net.dodian.uber.game.world.cache.`object`.ObjectDef
import net.dodian.uber.game.world.cache.`object`.ObjectLoader
import net.dodian.uber.game.world.cache.region.Region
import org.slf4j.LoggerFactory
import java.io.File

object WorldCacheBootstrap {
    private val logger = LoggerFactory.getLogger(WorldCacheBootstrap::class.java)

    @JvmStatic
    fun bootstrap() {
        val cacheDir = mysticCacheDir?.let(::File) ?: Cache.defaultCacheDirectory()
        logger.info("Loading mystic cache from {}", cacheDir.absolutePath)
        Cache.load(cacheDir)
        ObjectDef.loadConfig()
        Region.load()
        ObjectLoader().load()
        GameObjectData.init()
    }
}
