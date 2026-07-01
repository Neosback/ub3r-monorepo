package net.dodian.uber.game.npc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.model.entity.npc.NpcUpdating
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NpcFamilySpawnImportTest {
    private val exportRoot = Path.of("/Users/tylercovalt/Desktop/tarnish-main/game-server/data")
    private val gson = Gson()

    @Test
    fun `all live exported spawns exist in Kotlin family modules after id conversion`() {
        val expected = exportedLiveSpawnKeys()
        val actual = kotlinSpawnKeys()

        assertEquals(1321, expected.size)
        assertTrue(actual.containsAll(expected), "Kotlin family spawns are missing exported live rows")
    }

    @Test
    fun `known migrated ids use final ids`() {
        val actual = kotlinSpawnKeys()

        assertTrue(actual.any { it.npcId == 3295 }, "Hero spawns should use 3295")
        assertFalse(actual.any { it.npcId == 3106 }, "Hero spawns should not use old id 3106")
        assertTrue(actual.any { it.npcId == 5420 }, "Watchman spawns should use 5420")
        assertFalse(actual.any { it.npcId == 3251 }, "Watchman spawns should not use old id 3251")
        assertTrue(actual.any { it.npcId == 1790 }, "Gerrant spawns should use 1790")
        assertFalse(actual.any { it.npcId == 1027 }, "Gerrant spawns should not use old id 1027")
        assertTrue(actual.any { it.npcId == 10681 }, "Aubury spawns should use 10681")
        assertFalse(actual.any { it.npcId == 637 }, "Aubury spawns should not use old id 637")
    }

    @Test
    fun `makeover mage registers both clickable ids and keeps stable spawn id`() {
        assertTrue(MakeoverMage.definition.npcIds.contains(1306))
        assertTrue(MakeoverMage.definition.npcIds.contains(1307))
        assertEquals(1306, MakeoverMage.spawns.single().npcId)
    }

    @Test
    fun `makeover mage update path does not mutate server npc id`() {
        val source = Files.readString(Path.of("src/main/java/net/dodian/uber/game/model/entity/npc/NpcUpdating.java"))

        assertFalse(source.contains("npc.setId(player.getGender()"))
        assertTrue(source.contains("displayIdFor(player, npc)"))
    }

    @Test
    fun `generated family overrides do not emit known placeholder values`() {
        val root = Path.of("src/main/kotlin/net/dodian/uber/game/npc")
        Files.walk(root).use { paths ->
            paths
                .filter { it.toString().endsWith(".kt") }
                .filter { isFamilyContentFile(it) }
                .forEach { path ->
                    val source = Files.readString(path)
                    assertFalse(source.contains("hitpoints = 0"), "$path emits zero hitpoints")
                    assertFalse(source.contains("examine = \"no name\""), "$path emits placeholder examine")
                    assertFalse(source.contains("AUTO-GENERATED"), "$path contains generated marker comments")
                    assertFalse(source.contains("combatLevel ="), "$path emits display-facing combat level as runtime data")
                    assertFalse(source.contains("attackSpeed ="), "$path emits attack speed before combat consumes it")
                    assertFalse(source.contains("aggressive ="), "$path emits aggression before combat consumes it")
                    assertFalse(source.contains("moveChance"), "$path emits movement chance before NPC movement is implemented")
                    assertFalse(Regex("""private val \w+Family""").containsMatchIn(source), "$path uses unnecessary family backing val")
                }
        }
    }

    @Test
    fun `generated family spawns use named facing constants`() {
        val root = Path.of("src/main/kotlin/net/dodian/uber/game/npc")
        val numericFace = Regex("""face = \d+""")
        Files.walk(root).use { paths ->
            paths
                .filter { it.toString().endsWith(".kt") }
                .filter { isFamilyContentFile(it) }
                .forEach { path ->
                    val source = Files.readString(path)
                    assertFalse(numericFace.containsMatchIn(source), "$path emits numeric spawn facing")
                }
        }
    }

    @Test
    fun `aubury keeps cache dialogue identity and runtime values explicit`() {
        val source = Files.readString(Path.of("src/main/kotlin/net/dodian/uber/game/npc/Aubury.kt"))

        assertFalse(source.contains("size = 1"))
        assertTrue(source.contains("name = \"Aubury\""))
        assertTrue(source.contains("runtime {"))
        assertTrue(source.contains("deathAnimation = 2304"))
    }

    @Test
    fun `imported runtime stats are server definitions not cache overrides`() {
        val cowServer = Cow.definition.runtimeDefinitions.single()
        val cowCache = Cow.definition.cacheOverrides.single()

        assertEquals(8, cowServer.hitpoints)
        assertEquals(1, cowServer.attack)
        assertEquals("Meow meow I am a cow!", cowCache.examine)
        assertNull(cowCache.size)
    }

    @Test
    fun `content resolves makeover mage ids`() {
        val content1306 = ContentModuleIndex.npcContents.firstOrNull { it.npcIds.contains(1306) }
        val content1307 = ContentModuleIndex.npcContents.firstOrNull { it.npcIds.contains(1307) }

        assertNotNull(content1306)
        assertEquals(content1306, content1307)
    }

    private fun exportedLiveSpawnKeys(): Set<SpawnKey> {
        val rows = readJsonRows(exportRoot.resolve("Ubers mysql as json export/npc_Spawn.json"))
        val oldToNew = readOldToNew()
        return rows
            .asSequence()
            .filter { it.int("live") == 1 }
            .map {
                val oldId = it.int("id")
                SpawnKey(
                    npcId = oldToNew[oldId] ?: oldId,
                    x = it.int("x"),
                    y = it.int("y"),
                    z = it.int("height"),
                )
            }
            .toSet()
    }

    private fun kotlinSpawnKeys(): Set<SpawnKey> =
        ContentModuleIndex.npcModules
            .filterIsInstance<NpcSpawnSource>()
            .flatMap { it.spawns }
            .map { SpawnKey(it.npcId, it.x, it.y, it.z) }
            .toSet()

    private fun readOldToNew(): Map<Int, Int> {
        val pattern = Regex("-?\\d+")
        return Files.readAllLines(exportRoot.resolve("def/npc/oldtonew.txt"))
            .mapNotNull { line ->
                val values = pattern.findAll(line).map { it.value.toInt() }.toList()
                values.takeIf { it.size >= 2 }?.let { it[0] to it[1] }
            }
            .toMap()
    }

    private fun readJsonRows(path: Path): List<Map<String, Any>> {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        return gson.fromJson(Files.readString(path), type)
    }

    private fun Map<String, Any>.int(key: String): Int {
        val value = this[key] ?: return 0
        return when (value) {
            is Number -> value.toInt()
            else -> value.toString().toInt()
        }
    }

    private fun isFamilyContentFile(path: Path): Boolean {
        val fileName = path.fileName.toString()
        return fileName !in setOf(
            "NpcContent.kt",
            "NpcDefinitionOverride.kt",
            "NpcDefinitionRepository.kt",
            "NpcFamilyDsl.kt",
            "NpcSpawnDef.kt",
        )
    }

    data class SpawnKey(
        val npcId: Int,
        val x: Int,
        val y: Int,
        val z: Int,
    )
}
