package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.engine.systems.cache.CacheVarbitDefinition
import net.dodian.uber.game.model.entity.player.Client
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NpcClientMorphServiceTest {
    @AfterEach
    fun tearDown() {
        NpcClientConfigService.initialize(emptyMap())
        NpcClientMorphService.initialize(emptyMap())
        NpcClientConfigService.clearForTests()
    }

    @Test
    fun `varbit backed morph packs index into underlying varp`() {
        val client = testClient()
        NpcClientConfigService.initialize(
            mapOf(12 to CacheVarbitDefinition(id = 12, varp = 40, leastSignificantBit = 3, mostSignificantBit = 5))
        )
        NpcClientMorphService.initialize(
            mapOf(555 to morphNpc(id = 555, transformVarbit = 12, children = listOf(555, 222, 333)))
        )

        assertTrue(NpcClientMorphService.setMorphIndex(client, 555, 2))

        assertEquals(16, NpcClientConfigService.currentVarpForTests(client, 40))
    }

    @Test
    fun `varp backed morph writes raw config index`() {
        val client = testClient()
        NpcClientMorphService.initialize(
            mapOf(123 to morphNpc(id = 123, transformVarp = 77, children = listOf(123, 124)))
        )

        assertTrue(NpcClientMorphService.setMorphIndex(client, 123, 1))

        assertEquals(1, NpcClientConfigService.currentVarpForTests(client, 77))
    }

    @Test
    fun `invalid child index does not write config`() {
        val client = testClient()
        NpcClientConfigService.initialize(
            mapOf(12 to CacheVarbitDefinition(id = 12, varp = 40, leastSignificantBit = 3, mostSignificantBit = 5))
        )
        NpcClientMorphService.initialize(
            mapOf(555 to morphNpc(id = 555, transformVarbit = 12, children = listOf(555, -1)))
        )

        assertFalse(NpcClientMorphService.setMorphIndex(client, 555, 1))

        assertEquals(0, NpcClientConfigService.currentVarpForTests(client, 40))
    }

    @Test
    fun `npc without morph metadata is a safe no-op`() {
        val client = testClient()
        NpcClientMorphService.initialize(mapOf(555 to CacheNpcDefinition(id = 555, name = "Monk")))

        assertFalse(NpcClientMorphService.setMorphIndex(client, 555, 1))
    }

    private fun morphNpc(
        id: Int,
        transformVarbit: Int = -1,
        transformVarp: Int = -1,
        children: List<Int>,
    ): CacheNpcDefinition =
        CacheNpcDefinition(
            id = id,
            name = "Morph",
            transformVarbit = transformVarbit,
            transformVarp = transformVarp,
            transformChildren = children,
            transformFallbackChild = children.lastOrNull() ?: -1,
        )

    @Test
    fun `getSize returns morphed npc definition size for player`() {
        val client = testClient()
        NpcClientMorphService.initialize(
            mapOf(555 to CacheNpcDefinition(id = 555, name = "Giant", size = 3))
        )

        assertEquals(1, client.size)

        client.playerNpc = 555

        assertEquals(3, client.size)
    }

    private fun testClient(): Client = Client(null, 1)
}
