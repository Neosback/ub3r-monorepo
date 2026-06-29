package net.dodian.uber.game.npc

import net.dodian.uber.game.engine.systems.interaction.npcs.NpcContentRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NpcContentProfileRegistryTest {
    @AfterEach
    fun tearDown() {
        NpcContentRegistry.resetForTests()
    }

    @Test
    fun `profile handler wins over global handler for same npc id`() {
        NpcContentRegistry.clearForTests()
        NpcContentRegistry.register(definition("global-aubury", 11435))
        NpcContentRegistry.register(definition("varrock-aubury", 11435, "aubury.varrock"))

        assertEquals("varrock-aubury", NpcContentRegistry.get(11435, "aubury.varrock")?.name)
        assertEquals("global-aubury", NpcContentRegistry.get(11435, "aubury.yanille")?.name)
    }

    private fun definition(name: String, npcId: Int, vararg profiles: String): NpcContentDefinition =
        NpcContentDefinition(
            name = name,
            npcIds = intArrayOf(npcId),
            profiles = profiles.toSet(),
            onFirstClick = { _, _ -> true },
        )

}
