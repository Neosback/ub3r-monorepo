package net.dodian.uber.game.npc

import java.nio.file.Path
import java.nio.file.Files
import net.dodian.uber.game.item.ItemDefinitionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class DefinitionAndSpawnRepositoryTest {
    private val project = Path.of(System.getProperty("user.dir"))
    private val server = if (project.fileName.toString() == "game-server") project else project.resolve("game-server")

    @Test
    fun `definitions and converted Uber spawns load without MySQL`() {
        val definitions = NpcDefinitionRepository.load(server.resolve("data/cache"), server.resolve("data/def/npc"))
        assertEquals("Banker", definitions[1613]?.name)
        assertNotNull(definitions[1618])
        val spawns = NpcSpawnRepository.load(server.resolve("data/def/npc/spawns")) { definitions.containsKey(it) }
        assertTrue(spawns.any { it.npcId == 1613 && it.x == 2615 && it.y == 3094 && it.z == 0 })
        assertTrue(spawns.none { it.npcId == 394 || it.npcId == 395 })
        assertTrue(spawns.any { it.npcId == 1618 })
        assertTrue(spawns.any { it.npcId == 6080 })
        assertEquals(1_342, spawns.size)
        assertTrue(ItemDefinitionRepository.load(server.resolve("data/def/item"), server.resolve("data/cache")).size >= 26_988)

        val mapping = Files.readString(server.resolve("data/def/npc/oldtonew.txt"))
        assertTrue(mapping.lineSequence().any { it.trim() == "394=1613" })
        assertTrue(mapping.lineSequence().any { it.trim() == "395=1618" })
        val source = Files.readString(server.resolve("data/def/npc/spawn-import-source.json"))
        assertFalse(source.contains("npc_spawns.json"), "Tarnish spawn locations must never be imported")
    }
}