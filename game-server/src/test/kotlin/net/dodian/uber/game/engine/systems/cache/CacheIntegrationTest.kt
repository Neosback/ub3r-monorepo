package net.dodian.uber.game.engine.systems.cache

import java.nio.file.Path
import net.dodian.cache.objects.GameObjectData
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@Tag("cache-integration")
class CacheIntegrationTest {
    private val project = Path.of(System.getProperty("user.dir"))
    private val server = if (project.fileName.toString() == "game-server") project else project.resolve("game-server")

    @Test
    fun `manifest and complete Tarnish cache decode`() {
        val cache = server.resolve("data/cache")
        CacheManifestValidator.validateOrThrow(cache, server.resolve("data/def/cache-manifest.json"))
        CacheStore(cache).open().use { store ->
            val objects = ObjectDefinitionDecoder.decode(store)
            assertEquals(50_047, objects.definitionCount)
            assertEquals(50_047, objects.definitions.size)
            assertTrue(NpcCacheDefinitionDecoder.decode(store).size >= 11_000)
            assertTrue(ItemCacheDefinitionDecoder.decode(store).size >= 28_000)
            assertTrue(AnimationDefinitionDecoder.decode(store).isNotEmpty())
            val decoder = MapDecoder(store)
            val regions = decoder.decodeIndexEntries()
            assertEquals(2_179, regions.size)
            assertTrue(regions.all { decoder.decodeRegion(it).tileGrid != null })
            GameObjectData.replaceDefinitions(objects.definitions)
        }
    }
}