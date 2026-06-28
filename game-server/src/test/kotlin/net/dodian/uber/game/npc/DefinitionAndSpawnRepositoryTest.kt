package net.dodian.uber.game.npc

import java.nio.file.Path
import java.nio.file.Files
import net.dodian.uber.game.item.ItemDefinitionRepository
import net.dodian.uber.game.objects.ObjectSpawnRepository
import net.dodian.cache.objects.GameObjectData
import net.dodian.utilities.Geometry
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditObject
import net.dodian.uber.game.engine.systems.cache.CacheCollisionAuditStore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class DefinitionAndSpawnRepositoryTest {
    private val project = Path.of(System.getProperty("user.dir"))
    private val server = if (project.fileName.toString() == "game-server") project else project.resolve("game-server")

    @Test
    fun `definitions and converted Uber spawns load without MySQL`() {
        val definitions = NpcDefinitionRepository.load(server.resolve("data/cache"))
        assertEquals("Banker", definitions[1613]?.name)
        assertNotNull(definitions[1618])
        val spawns = NpcSpawnRepository.load(NpcSpawnRepository.resolveSpawnsPath(), definitions) { definitions.containsKey(it) }
        assertTrue(spawns.any { it.npcId == 1613 && it.x == 2615 && it.y == 3094 && it.z == 0 })
        assertTrue(spawns.none { it.npcId == 394 || it.npcId == 395 })
        assertTrue(spawns.any { it.npcId == 1618 })
        assertTrue(spawns.any { it.npcId == 6080 })
        val dad = spawns.single { it.npcId == 4130 && it.x == 2541 && it.y == 3092 && it.z == 0 }
        assertEquals(3, dad.face)
        assertEquals("dad.yanille", dad.profile)
        assertEquals(1_342, spawns.size)
        assertTrue(ItemDefinitionRepository.load(server.resolve("data/def/item"), server.resolve("data/cache")).size >= 26_988)

    }

    @Test
    fun `npc definition overrides apply over cache definitions`() {
        val definitions = NpcDefinitionRepository.load(server.resolve("data/cache"))
        val aubury = definitions[10681]
        assertNotNull(aubury)
        assertEquals("Aubury", aubury!!.name)
        assertArrayEquals(arrayOf("Talk-to", null, "Trade", "Teleport", null), aubury.actions)
    }

    @Test
    fun `jsonc name-based spawns resolve with comments trailing commas and profile`() {
        val tempDir = Files.createTempDirectory("spawns-test")
        val spawnJson = """
            // comments and trailing commas are supported for authoring
            {
              "schemaVersion": 1,
              "family": "test_family",
              "groups": [
                {
                  "name": "Banker",
                  "profile": "banker.test",
                  "spawns": [
                    { "x": 3200, "y": 3200, "plane": 0 },
                  ],
                },
              ],
            }
        """.trimIndent()
        Files.writeString(tempDir.resolve("test_spawn.jsonc"), spawnJson)

        val definitions = NpcDefinitionRepository.load(server.resolve("data/cache"))
        val spawns = NpcSpawnRepository.load(tempDir, definitions) { true }
        assertEquals(1, spawns.size)
        val resolvedNpcId = spawns[0].npcId
        assertEquals("Banker", definitions[resolvedNpcId]?.name)
        assertEquals(3200, spawns[0].x)
        assertEquals(3200, spawns[0].y)
        assertEquals("banker.test", spawns[0].profile)
        
        Files.deleteIfExists(tempDir.resolve("test_spawn.jsonc"))
        Files.deleteIfExists(tempDir)
     }

     @Test
     fun `custom objects load and overrides register correctly`() {
         val tempDir = Files.createTempDirectory("objects-test")
         val objJson = """
             {
               "schemaVersion": 1,
               "region": "test_region",
               "objects": [
                 {
                   "id": 9999,
                   "name": "Custom Overridden Bench",
                   "x": 3200,
                   "y": 3200,
                   "z": 0,
                   "type": 10,
                   "rotation": "EAST",
                   "sizeX": 3,
                   "sizeY": 2,
                   "solid": true,
                   "walkable": false
                 }
               ]
             }
         """.trimIndent()
         Files.writeString(tempDir.resolve("test_objects.json"), objJson)

         val objects = ObjectSpawnRepository.load(tempDir)
         assertEquals(1, objects.size)
         val obj = objects[0]
         assertEquals(9999, obj.id)
         assertEquals(3200, obj.x)
         assertEquals(3200, obj.y)
         assertEquals(3, obj.face)

         val data = GameObjectData.forId(9999)
         assertEquals("Custom Overridden Bench", data.name)
         assertEquals(3, data.sizeX)
         assertEquals(2, data.sizeY)
         assertTrue(data.isSolid())
         assertFalse(data.isWalkable())

         Files.deleteIfExists(tempDir.resolve("test_objects.json"))
         Files.deleteIfExists(tempDir)
     }

     @Test
     fun `morphic objects resolve successfully via childIds`() {
         // Create a base morphic object definition with childIds
         val baseData = GameObjectData(
             id = 10355,
             name = "Bank booth",
             description = "Base booth",
             sizeX = 1,
             sizeY = 1,
             solid = true,
             impenetrable = true,
             hasActionsFlag = true,
             decoration = false,
             walkType = 2,
             childIds = intArrayOf(10356)
         )
         GameObjectData.addDefinition(baseData)

         // Publish a mock collision object representing the spawned base object in the world tile (3200, 3200)
         val list =
             listOf(
                 CacheCollisionAuditObject(
                     objectId = 10355,
                     type = 10,
                     rotation = 2,
                     x = 3200,
                     y = 3200,
                     rawPlane = 0,
                     effectivePlane = 0,
                     regionId = CacheCollisionAuditStore.regionId(3200, 3200),
                 ),
             )
         CacheCollisionAuditStore.publish(emptyList(), mapOf(CacheCollisionAuditStore.regionId(3200, 3200) to list))

         // Verify that querying Geometry.getObject with the morphed ID (10356) correctly matches the base object!
         val resolved = Geometry.getObject(10356, 3200, 3200, 0)
         assertNotNull(resolved)
         assertEquals(10356, resolved!!.id)
         assertEquals(2, resolved.face)
         assertEquals(10, resolved.type)
     }
}
