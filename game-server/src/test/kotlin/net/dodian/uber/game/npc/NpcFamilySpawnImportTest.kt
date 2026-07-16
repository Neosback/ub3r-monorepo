package net.dodian.uber.game.npc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.api.plugin.skills.SkillNpcClickBinding
import net.dodian.uber.game.engine.systems.action.PolicyPreset
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.model.entity.npc.NpcUpdating
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.persistence.audit.ConsoleAuditLog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NpcFamilySpawnImportTest {
    private val exportRoot = Path.of("/Users/tylercovalt/Desktop/RSPS/tarnish-main/game-server/data")
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

        assertTrue(actual.any { it.npcId == 3258 }, "Druid spawns should use current-cache id 3258")
        assertFalse(actual.any { it.npcId == 3098 }, "Druid spawns should not use old id 3098")
        assertTrue(actual.any { it.npcId == 3295 }, "Hero spawns should use 3295")
        assertFalse(actual.any { it.npcId == 3106 }, "Hero spawns should not use old id 3106")
        assertTrue(actual.any { it.npcId == 5420 }, "Watchman spawns should use 5420")
        assertFalse(actual.any { it.npcId == 3251 }, "Watchman spawns should not use old id 3251")
        assertTrue(actual.any { it.npcId == 1790 }, "Gerrant spawns should use 1790")
        assertFalse(actual.any { it.npcId == 1027 }, "Gerrant spawns should not use old id 1027")
        assertTrue(actual.any { it.npcId == 11435 }, "Varrock Aubury spawn should use clickable id 11435")
        val yanilleSpawn = Aubury.spawns.first { it.profile == "aubury.yanille" }
        assertEquals(11435, yanilleSpawn.npcId, "Yanille Aubury spawn should use cache-visible Aubury id 11435")
        assertEquals("aubury.yanille", yanilleSpawn.profile)
        assertFalse(actual.any { it.npcId == 10681 }, "Aubury spawns should not use unclickable id 10681")
        assertFalse(actual.any { it.npcId == 637 }, "Aubury spawns should not use old id 637")
        assertEquals(11433, Sedridor.spawns.single().npcId, "Sedridor spawn should use cache-visible Archmage Sedridor id 11433")
        assertFalse(actual.any { it.npcId == 5034 }, "Sedridor spawn should not use blank cache id 5034")
    }

    @Test
    fun `restored npc families bind current cache option slots`() {
        assertEquals("talk-to", GnomeTrainer.definition.optionLabels[1])
        assertEquals("talk-to", CustomsOfficier.definition.optionLabels[1])
        assertEquals("pay-fare", CustomsOfficier.definition.optionLabels[3])
        assertEquals("talk-to", ArmourSalesman.definition.optionLabels[1])
        assertEquals("trade", ArmourSalesman.definition.optionLabels[3])
        assertEquals(9, GnomeTrainer.spawns.size)
    }

    @Test
    fun `druid migration removes stale client identity override and examine loot side effect`() {
        val clientNpcDefinitions = Files.readString(Path.of("../game-client/src/main/java/com/osroyale/NpcDefinition.java"))
        val playerSource = Files.readString(Path.of("src/main/java/net/dodian/uber/game/model/entity/player/Player.java"))
        val examineBody = playerSource.substringAfter("public void examineNpc").substringBefore("public void examineObject")

        assertFalse(clientNpcDefinitions.contains("case 3258:"))
        assertFalse(examineBody.contains("checkLoot"))
        assertEquals(3258, Druid.primaryId)
        assertTrue(Druid.spawns.all { it.npcId == 3258 })
    }

    @Test
    fun `interface audit identifies bank and read only bank style views`() {
        val client = Client(null, 1)
        client.bankStyleViewOpen = true
        client.bankStyleViewTitle = "Loot preview"
        assertTrue(ConsoleAuditLog.interfaceDetails(client, 60000).contains("Bank-style read-only view"))
        assertTrue(ConsoleAuditLog.interfaceDetails(client, 60000).contains("Loot preview"))

        client.bankStyleViewOpen = false
        client.IsBanking = true
        assertTrue(ConsoleAuditLog.interfaceDetails(client, 60000).contains("Player bank"))
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
    fun `npc transform helpers use appearance updates without mutating server id`() {
        val source = Files.readString(Path.of("src/main/java/net/dodian/uber/game/model/entity/npc/Npc.java"))

        assertTrue(source.contains("void transformTo(int npcId)"))
        assertTrue(source.contains("void clearTransform()"))
        assertTrue(source.contains("setTransformedNpcId"))
        assertFalse(source.contains("transformTo(int npcId) {\n        setId"))
    }

    @Test
    fun `npc family handlers do not self reference object during delegation`() {
        val root = Path.of("src/main/kotlin/net/dodian/uber/game/npc")
        Files.walk(root).use { paths ->
            paths
                .filter { it.toString().endsWith(".kt") }
                .filter { isFamilyContentFile(it) }
                .forEach { path ->
                    val source = Files.readString(path)
                    val objectName = Regex("""internal object\s+(\w+)\s*:\s*NpcFamily\s+by\s+npcFamily""")
                        .find(source)
                        ?.groupValues
                        ?.get(1)
                        ?: return@forEach
                    val unsafeReference = Regex("""(?:handler\s*=\s*)?$objectName::""")
                    assertFalse(unsafeReference.containsMatchIn(source), "$path self-references $objectName while building its delegate")
                }
        }
    }

    @Test
    fun `monk restores old disappear effect without fake players or morph test`() {
        val monkSource = Files.readString(Path.of("src/main/kotlin/net/dodian/uber/game/npc/Monk.kt"))

        assertTrue(monkSource.contains("SendCameraShake(3, 2, 3, 2)"))
        assertTrue(monkSource.contains("visibleWhen(::isYanilleMonkVisible)"))
        assertTrue(monkSource.contains("client.quests[0]++"))
        assertFalse(monkSource.contains("NpcClientMorphService.setMorphIndex"))
        assertFalse(monkSource.contains("DisplayOnlyPlayerService"))
        assertFalse(monkSource.contains("npc.transformTo("))
        assertFalse(monkSource.contains("npc.setId("))
    }

    @Test
    fun `monk spawn hides for players who clicked it`() {
        val spawn = Monk.spawns.single()
        val client = Client(null, 1)

        assertTrue(spawn.condition(client))
        client.quests[0] = 1
        assertFalse(spawn.condition(client))
    }

    @Test
    fun `monk owns id 555 and shopkeeper does not`() {
        assertTrue(Monk.definition.npcIds.contains(555))
        assertFalse(ShopKeeper.definition.npcIds.contains(555))
        assertTrue(ShopKeeper.definition.npcIds.contains(2813))
        assertSame(Monk.definition, ContentModuleIndex.npcContents.first { it.npcIds.contains(555) })
    }

    @Test
    fun `shopkeeper trade handler uses cache-visible third option`() {
        assertFalse(ShopKeeper.definition.optionLabels.containsKey(2))
        assertEquals("trade", ShopKeeper.definition.optionLabels[3])
    }

    @Test
    fun `npc client option validator rejects handlers for missing cache slots`() {
        val report = NpcClientOptionValidator.inspect(
            mapOf(555 to rawNpc(555, "Monk", "Talk-to", null, null, null, null)),
            listOf(
                NpcContentDefinition(
                    name = "Bad shop",
                    npcIds = intArrayOf(555),
                    optionLabels = mapOf(2 to "trade"),
                    onSecondClick = { _, _ -> true },
                )
            )
        )

        assertTrue(report.optionViolations.single().contains("npc=555"))
        assertTrue(report.optionViolations.single().contains("option=2"))
    }

    @Test
    fun `npc client option validator accepts monk first click and shopkeeper third click`() {
        val report = NpcClientOptionValidator.inspect(
            mapOf(
                555 to rawNpc(555, "Monk", "Talk-to", null, null, null, null),
                2813 to rawNpc(2813, "Shop keeper", "Talk-to", null, "Trade", null, null),
            ),
            listOf(Monk.definition, ShopKeeper.definition)
        )

        assertTrue(report.optionViolations.isEmpty())
    }

    @Test
    fun `npc client option validator warns but does not fail on raw name mismatch`() {
        val report = NpcClientOptionValidator.inspect(
            mapOf(555 to rawNpc(555, "Monk", "Talk-to", null, null, null, null)),
            listOf(
                NpcContentDefinition(
                    name = "Blessed monk",
                    npcIds = intArrayOf(555),
                    optionLabels = mapOf(1 to "talk-to"),
                    onFirstClick = { _, _ -> true },
                )
            )
        )

        assertTrue(report.optionViolations.isEmpty())
        assertTrue(report.nameWarnings.single().contains("rawName='Monk'"))
    }

    @Test
    fun `npc client option validator reports live identity and handler gaps`() {
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = mapOf(3098 to rawNpc(3098, "Stonemason", "Talk-to", null, "Trade", null, null)),
            contents = listOf(
                NpcContentDefinition(name = "Druid", npcIds = intArrayOf(3098))
            ),
            modules = listOf(
                object : NpcModule {
                    override val definition = NpcContentDefinition(name = "Druid", npcIds = intArrayOf(3098))
                }
            ),
            spawns = listOf(NpcSpawnDef(3098, 2884, 3430)),
        )

        assertEquals("Stonemason", report.identityMismatches.single().cacheName)
        assertEquals(setOf(1, 3), report.missingVisibleHandlers.map { it.option }.toSet())
        assertEquals(1, report.liveCapabilities.single().liveSpawnCount)
    }

    @Test
    fun `npc client option validator recognizes skill owned actions`() {
        val skillBinding = SkillNpcClickBinding(
            preset = PolicyPreset.GATHERING,
            option = 1,
            npcIds = intArrayOf(1510),
            handler = { true },
        )
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = mapOf(1510 to rawNpc(1510, "Fishing spot", "Cage", null, null, null, null)),
            contents = emptyList(),
            spawns = listOf(NpcSpawnDef(1510, 2800, 3400)),
            skillNpcBindings = listOf(skillBinding),
        )

        assertTrue(report.missingVisibleHandlers.isEmpty())
        assertEquals("skill", report.liveCapabilities.single().dispatchOwners[1])
    }

    @Test
    fun `npc client option validator reports effective client override conflicts`() {
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = mapOf(3258 to rawNpc(3258, "Druid", "Talk-to", "Attack", null, null, null)),
            contents = emptyList(),
            spawns = listOf(NpcSpawnDef(3258, 2884, 3430)),
            effectiveClientOverrides = mapOf(
                3258 to NpcEffectiveClientOverride(
                    id = 3258,
                    name = "Farming store",
                    actions = arrayOf("Open", null, null, null, null),
                )
            ),
        )

        assertEquals("Farming store", report.effectiveClientOverrideConflicts.single().effectiveName)
    }

    @Test
    fun `npc client override parser resolves stacked ids names and action slots`() {
        val overrides = NpcClientOptionValidator.parseEffectiveClientOverrides(
            source = """
                switch (npcId) {
                    case 3258:
                    case 3259:
                        entityDef.name = "Farming store";
                        entityDef.actions = new String[5];
                        entityDef.actions[0] = "Open";
                        entityDef.actions[2] = "Trade";
                        break;
                }
            """.trimIndent(),
            rawDefinitions = mapOf(
                3258 to rawNpc(3258, "Druid", "Talk-to", "Attack", null, null, null),
                3259 to rawNpc(3259, "Druid", "Talk-to", "Attack", null, null, null),
            ),
        )

        assertEquals(setOf(3258, 3259), overrides.keys)
        assertEquals("Farming store", overrides.getValue(3258).name)
        assertEquals(listOf("Open", null, "Trade", null, null), overrides.getValue(3259).actions?.toList())
    }

    @Test
    fun `npc client option validator rejects registered content ids missing from raw cache`() {
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = emptyMap(),
            contents = listOf(
                NpcContentDefinition(
                    name = "Missing content",
                    npcIds = intArrayOf(999999),
                )
            ),
        )

        assertTrue(report.failures.single().contains("registered in Kotlin content"))
        assertTrue(report.failures.single().contains("999999"))
    }

    @Test
    fun `npc client option validator rejects live spawn ids missing from raw cache`() {
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = emptyMap(),
            contents = emptyList(),
            spawns = listOf(NpcSpawnDef(npcId = 999999, x = 3200, y = 3200)),
        )

        assertTrue(report.failures.single().contains("Live Kotlin spawn npc=999999"))
    }

    @Test
    fun `npc client option validator warns but does not fail on label drift`() {
        val report = NpcClientOptionValidator.inspect(
            rawDefinitions = mapOf(555 to rawNpc(555, "Monk", "Talk-to", null, null, null, null)),
            contents = listOf(
                NpcContentDefinition(
                    name = "Monk",
                    npcIds = intArrayOf(555),
                    optionLabels = mapOf(1 to "bless"),
                    onFirstClick = { _, _ -> true },
                )
            ),
        )

        assertTrue(report.failures.isEmpty())
        assertTrue(report.warnings.single().contains("label='bless'"))
    }

    @Test
    fun `npc diagnostics writes cache and runtime validation reports`() {
        val reportsDir = Files.createTempDirectory("npc-diagnostics-test")
        val rawDefinitions = mapOf(
            555 to rawNpc(555, "Monk", "Talk-to", null, null, null, null),
            2813 to rawNpc(2813, "Shop keeper", "Talk-to", null, "Trade", null, null),
            11434 to rawNpc(11434, "Aubury", "Talk-to", null, "Trade", "Teleport", null),
            11435 to rawNpc(11435, "Aubury", "Talk-to", null, "Trade", "Teleport", null),
        )
        val validation = NpcClientOptionValidator.inspect(
            rawDefinitions = rawDefinitions,
            contents = listOf(Monk.definition, ShopKeeper.definition, Aubury.definition),
            modules = listOf(Monk, ShopKeeper, Aubury),
            spawns = listOf(
                NpcSpawnDef(555, 2604, 3092),
                NpcSpawnDef(2813, 3216, 3416),
                NpcSpawnDef(11434, 2594, 3104),
                NpcSpawnDef(11435, 3253, 3402),
            ),
        )

        val paths = NpcClientOptionValidator.writeReports(
            rawDefinitions = rawDefinitions,
            resolvedDefinitions = rawDefinitions,
            contents = listOf(Monk.definition, ShopKeeper.definition, Aubury.definition),
            modules = listOf(Monk, ShopKeeper, Aubury),
            spawns = listOf(
                NpcSpawnDef(555, 2604, 3092),
                NpcSpawnDef(2813, 3216, 3416),
                NpcSpawnDef(11434, 2594, 3104),
                NpcSpawnDef(11435, 3253, 3402),
            ),
            validation = validation,
            reportsDir = reportsDir,
        )

        val cacheJson = Files.readString(paths.cacheDefinitions)
        val runtimeJson = Files.readString(paths.runtimeValidation)
        assertTrue(cacheJson.contains("\"id\" : 555"))
        assertTrue(cacheJson.contains("\"name\" : \"Monk\""))
        assertTrue(cacheJson.contains("\"Trade\""))
        assertTrue(cacheJson.contains("\"Teleport\""))
        assertTrue(cacheJson.contains("\"transformVarbit\""))
        assertTrue(runtimeJson.contains("\"morphVariables\""))
        assertTrue(runtimeJson.contains("\"module\" : \"Monk\""))
        assertTrue(runtimeJson.contains("\"uniqueLiveSpawnIds\" : 4"))
        assertTrue(Files.readString(paths.migrationBacklog).contains("\"missingVisibleHandlers\""))
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
                    assertFalse(source.contains("runtime {"), "$path emits deprecated runtime block")
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
    fun `aubury keeps cache dialogue identity and server values explicit`() {
        val source = Files.readString(Path.of("src/main/kotlin/net/dodian/uber/game/npc/Aubury.kt"))

        assertFalse(source.contains("size = 1"))
        assertTrue(source.contains("server {"))
        assertTrue(source.contains("deathAnimation = 2304"))
        assertTrue(source.contains("examine = \"Runes are his passion.\""))
    }

    @Test
    fun `aubury uses cache-clickable ids and exposes trade plus teleport`() {
        assertTrue(Aubury.definition.npcIds.contains(11435))
        assertFalse(Aubury.definition.npcIds.contains(10681))
        assertEquals("trade", Aubury.definition.optionLabels[3])
        assertEquals("teleport", Aubury.definition.optionLabels[4])
    }

    @Test
    fun `imported server stats are server definitions not cache overrides`() {
        val cowServer = Cow.definition.serverDefinitions.single()
        val cowCache = Cow.definition.cacheOverrides.single()

        assertEquals(8, cowServer.hitpoints)
        assertEquals(1, cowServer.attack)
        assertEquals("Meow meow I am a cow!", cowCache.examine)
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
                val x = it.int("x")
                val y = it.int("y")
                val z = it.int("height")
                SpawnKey(
                    npcId = finalSpawnId(oldToNew[oldId] ?: oldId, x, y, z),
                    x = x,
                    y = y,
                    z = z,
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

    private fun finalSpawnId(npcId: Int, x: Int, y: Int, z: Int): Int =
        when {
            npcId == 10681 && x == 3253 && y == 3402 && z == 0 -> 11435
            npcId == 10681 && x == 2594 && y == 3104 && z == 0 -> 11435
            npcId == 5034 && x == 2590 && y == 3086 && z == 0 -> 11433
            npcId == 3098 -> 3258
            else -> npcId
        }

    private fun rawNpc(
        id: Int,
        name: String,
        first: String?,
        second: String?,
        third: String?,
        fourth: String?,
        fifth: String?,
    ): CacheNpcDefinition =
        CacheNpcDefinition(id = id, name = name, actions = arrayOf(first, second, third, fourth, fifth))

    private fun isFamilyContentFile(path: Path): Boolean {
        val fileName = path.fileName.toString()
        return fileName !in setOf(
            "NpcContent.kt",
            "NpcDefinitionOverride.kt",
            "NpcDefinitionRepository.kt",
            "NpcFamilyDsl.kt",
            "NpcClientOptionValidator.kt",
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
