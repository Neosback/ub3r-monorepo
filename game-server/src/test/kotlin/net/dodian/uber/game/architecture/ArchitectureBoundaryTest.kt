package net.dodian.uber.game.architecture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString

class ArchitectureBoundaryTest {
    private val sourceRoot: Path = Paths.get("src/main")
    private val sourceFiles: List<Path> by lazy {
        Files.walk(sourceRoot)
            .filter { Files.isRegularFile(it) }
            .filter { it.extension == "kt" || it.extension == "java" }
            .toList()
    }

    @Test
    fun `content layer does not import engine sync or net internals`() {
        val temporaryAllowListByFile = emptyMap<String, Set<String>>()
        val violations = sourceFiles
            .filter { it.toString().contains("/net/dodian/uber/game/content/") }
            .flatMap { file ->
                val normalizedPath = file.invariantSeparatorsPathString
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("import ")) return@mapIndexedNotNull null
                    if (!trimmed.contains("net.dodian.uber.game.engine.")) return@mapIndexedNotNull null
                    val allowedRuntimeBridge =
                        (
                            normalizedPath.endsWith("/content/skills/core/runtime/GatheringTask.kt") &&
                                (
                                    trimmed == "import net.dodian.uber.game.engine.tasking.GameTaskRuntime" ||
                                        trimmed == "import net.dodian.uber.game.engine.tasking.TaskHandle" ||
                                        trimmed == "import net.dodian.uber.game.engine.tasking.TaskPriority"
                                    )
                            ) ||
                            (
                                normalizedPath.endsWith("/content/skills/core/runtime/SkillingActionDsl.kt") &&
                                    trimmed == "import net.dodian.uber.game.engine.tasking.TaskPriority"
                                )
                    if (allowedRuntimeBridge) return@mapIndexedNotNull null
                    if (trimmed in (temporaryAllowListByFile[normalizedPath] ?: emptySet())) return@mapIndexedNotNull null
                    "${file}:${idx + 1} -> $trimmed"
                }
            }
        assertTrue(
            violations.isEmpty(),
            "Content must not import engine internals.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `systems layer does not import engine sync or net internals`() {
        val violations = sourceFiles
            .filter { it.toString().contains("/net/dodian/uber/game/systems/") }
            .flatMap { file ->
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("import ")) return@mapIndexedNotNull null
                    val forbidden = trimmed.contains("net.dodian.uber.game.engine.sync") ||
                        trimmed.contains("net.dodian.uber.game.engine.net")
                    if (!forbidden) return@mapIndexedNotNull null
                    "${file}:${idx + 1} -> $trimmed"
                }
            }
        assertTrue(
            violations.isEmpty(),
            "Systems must not import engine sync/net internals.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `engine layer does not import persistence`() {
        val violations = sourceFiles
            .filter { file ->
                Files.readAllLines(file).any { line ->
                    val pkg = line.trim()
                    pkg.startsWith("package net.dodian.uber.game.engine.loop") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.phases") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.sync") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.net") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.tasking") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.scheduler") ||
                        pkg.startsWith("package net.dodian.uber.game.engine.metrics")
                }
            }
            .flatMap { file ->
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("import ")) return@mapIndexedNotNull null
                    if (!trimmed.contains("net.dodian.uber.game.persistence")) return@mapIndexedNotNull null
                    "${file}:${idx + 1} -> $trimmed"
                }
            }
        assertTrue(
            violations.isEmpty(),
            "Engine/runtime must not import persistence directly.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `runtime interaction layers avoid direct database primitives`() {
        val runtimeRoots = listOf(
            "/net/dodian/uber/game/engine/",
            "/net/dodian/uber/game/systems/",
            "/net/dodian/uber/game/content/",
            "/net/dodian/uber/game/event/",
            "/net/dodian/uber/game/netty/listener/",
            "/net/dodian/uber/game/runtime/",
            "/net/dodian/uber/game/model/entity/",
        )
        val forbiddenSnippets = listOf(
            "dbConnection",
            "dbStatement",
            "getDbConnection(",
            "prepareStatement(",
            "createStatement(",
            "DriverManager.getConnection(",
        )

        val violations = sourceFiles
            .filter { file ->
                val normalized = file.invariantSeparatorsPathString
                runtimeRoots.any { normalized.contains(it) }
            }
            .flatMap { file ->
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("//")) {
                        return@mapIndexedNotNull null
                    }
                    if (!forbiddenSnippets.any { snippet -> trimmed.contains(snippet) }) {
                        return@mapIndexedNotNull null
                    }
                    "${file}:${idx + 1} -> $trimmed"
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Runtime interaction layers must avoid direct database primitives.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `source file path matches declared package`() {
        val violations = sourceFiles.mapNotNull { file ->
            val lines = Files.readAllLines(file)
            val packageLine = lines
                .asSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("package ") }
                ?: return@mapNotNull null

            val packageName = packageLine
                .removePrefix("package ")
                .trim()
                .removeSuffix(";")
                .replace("`", "")
            val packagePath = packageName.replace('.', '/')
            val filePath = file.invariantSeparatorsPathString
            val expectedPathSuffix = "$packagePath/${file.fileName}"
            if (filePath.endsWith(expectedPathSuffix)) return@mapNotNull null

            "$file -> declared '$packageName' expects '*$expectedPathSuffix'"
        }

        assertTrue(
            violations.isEmpty(),
            "Source file paths must align with declared package paths.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `legacy repackaged namespaces are removed`() {
        val removedToggleSymbols = setOf(
            "gameLoopEnabled",
            "interactionPipelineEnabled",
            "updatePrepEnabled",
            "synchronizationEnabled",
            "syncRootBlockCacheEnabled",
            "syncViewportSnapshotEnabled",
            "syncSkipEmptyNpcPacketEnabled",
            "syncPlayerActivityIndexEnabled",
            "syncSkipEmptyPlayerPacketEnabled",
            "syncPlayerTemplateCacheEnabled",
            "syncScratchBufferReuseEnabled",
            "syncAppearanceCacheEnabled",
            "playerSynchronizationEnabled",
            "syncPlayerRootDiffEnabled",
            "syncPlayerSelfOnlyEnabled",
            "syncPlayerIncrementalBuildEnabled",
            "syncPlayerFullRebuildFallbackEnabled",
            "syncPlayerReasonMetricsEnabled",
            "syncPlayerDesiredLocalsEnabled",
            "syncPlayerAdmissionQueueEnabled",
            "syncPlayerIncrementalAddsEnabled",
            "syncPlayerMovementFragmentCacheEnabled",
            "syncPlayerAllocationLightEnabled",
            "syncPlayerFragmentReuseEnabled",
            "syncPlayerStateValidationEnabled",
            "syncNpcActivityIndexEnabled",
            "farmingSchedulerEnabled",
            "zoneUpdateBatchingEnabled",
            "queueTasksEnabled",
            "opcode248HasExtra14ByteSuffix",
            "clientUiDeltaProcessorEnabled",
            "databaseConnectionProxyEnabled",
            "runtimePhaseTimingEnabled",
            "runtimeCycleLogEnabled",
            "clientUiTraceEnabled",
            "clientPacketTraceEnabled",
            "combatReactionDebugEnabled",
            "buttonTraceEnabled",
            "objectTraceEnabled",
            "smeltingTraceEnabled",
            "inboundOpcodeProfilingEnabled",
            "inboundOpcodeProfilingWarnMs",
        )
        val removedNpcManagerSymbols = setOf(
            "gnomeSpawn",
            "werewolfSpawn",
            "dagaRex",
            "dagaSupreme",
            "REQUIRED_HARDCODED_NPC_DEFINITIONS",
            "REQUIRED_HARDCODED_NPC_NAMES",
            "repairRequiredHardcodedDefinitions",
        )
        val removedSkillSymbols = setOf("SkillWIP", "skillById(", "skillByName(", "skillsEnabled(")

        val legacyPackageViolations = sourceFiles.mapNotNull { file ->
            val packageLine = Files.readAllLines(file)
                .asSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("package ") }
                ?: return@mapNotNull null
            val packageName = packageLine.removePrefix("package ").trim().removeSuffix(";")
            val fileName = file.fileName.toString()
            val isLegacy =
                packageName.startsWith("net.dodian.uber.game.content.entities") ||
                    packageName.startsWith("net.dodian.uber.game.systems.ui.interfaces") ||
                    packageName.startsWith("net.dodian.uber.game.systems.ui.dialogue.modules") ||
                    (packageName == "net.dodian.uber.game.skills.farming" && fileName == "FarmingProcessor.kt") ||
                    (packageName == "net.dodian.uber.game.skills.thieving.plunder" && fileName == "PlunderDoorProcessor.kt") ||
                    packageName.startsWith("net.dodian.jobs") ||
                    (packageName == "net.dodian.utilities" && (
                        fileName == "Database.kt" ||
                            fileName == "DatabaseConfig.kt" ||
                            fileName == "DatabaseInitializer.kt" ||
                            fileName == "DotEnv.kt"
                        ))
            if (!isLegacy) return@mapNotNull null
            "${file} -> $packageName"
        }

        val legacyPathViolations = sourceFiles.mapNotNull { file ->
            val normalized = file.invariantSeparatorsPathString
            val isLegacyPath =
                    normalized.contains("/net/dodian/uber/game/content/entities/") ||
                    normalized.contains("/net/dodian/uber/game/systems/ui/interfaces/") ||
                    normalized.contains("/net/dodian/uber/game/systems/ui/dialogue/modules/") ||
                    normalized.endsWith("/net/dodian/uber/game/skills/farming/FarmingProcessor.kt") ||
                    normalized.endsWith("/net/dodian/uber/game/skills/thieving/plunder/PlunderDoorProcessor.kt") ||
                    normalized.contains("src/main/java/net/dodian/jobs/") ||
                    normalized.endsWith("/net/dodian/uber/game/SkillWIP.kt") ||
                    normalized.endsWith("/net/dodian/utilities/Database.kt") ||
                    normalized.endsWith("/net/dodian/utilities/DatabaseConfig.kt") ||
                    normalized.endsWith("/net/dodian/utilities/DatabaseInitializer.kt") ||
                    normalized.endsWith("/net/dodian/utilities/DotEnv.kt")
            if (!isLegacyPath) return@mapNotNull null
            normalized
        }

        val legacyReferenceViolations = sourceFiles.flatMap { file ->
            val normalized = file.invariantSeparatorsPathString
            Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                val trimmed = line.trim()
                val isLegacyLoopMarker =
                    (normalized.endsWith("/content/skills/woodcutting/WoodcuttingService.kt") ||
                        normalized.endsWith("/content/skills/mining/MiningService.kt")) &&
                        (trimmed.contains("nextSwingAnimationCycle") ||
                            trimmed.contains("nextResourceCycle") ||
                            trimmed.contains("PlayerActionController.start("))
                val isWave2LegacyLoopMarker =
                    normalized.endsWith("/systems/action/SkillingActionService.kt") &&
                        (trimmed.contains("type = PlayerActionType.FISHING") ||
                            trimmed.contains("type = PlayerActionType.FLETCHING") ||
                            trimmed.contains("type = PlayerActionType.COOKING"))
                val isLegacyPlayerArrayAccess =
                    trimmed.contains("PlayerHandler.players[") &&
                        !normalized.endsWith("/model/entity/player/Client.java")
                val isHardCutLegacyNaming =
                    trimmed.contains("PlayerHandler") ||
                        trimmed.contains("ShopHandler") ||
                        trimmed.contains("DoorHandler")
                val isLegacyFrameApiUsage =
                    (
                        trimmed.contains("sendFrame164(") ||
                            trimmed.contains("sendFrame200(") ||
                            trimmed.contains("sendFrame246(")
                        ) &&
                        !normalized.endsWith("/model/entity/player/Client.java")
                val isLegacyClientItemHelperUsage =
                    (
                        trimmed.contains("GetItemName(") ||
                            trimmed.contains("GetItemSlot(") ||
                            trimmed.contains("IsItemInBag(") ||
                            trimmed.contains("AreXItemsInBag(") ||
                            trimmed.contains("GetNotedItem(") ||
                            trimmed.contains("GetUnnotedItem(")
                        ) &&
                        !normalized.endsWith("/model/entity/player/Client.java")
                val isManualCoreSkillControllerMarker =
                    (normalized.endsWith("/systems/action/SmithingActionService.kt") ||
                        normalized.endsWith("/content/skills/smithing/SmeltingActionService.kt")) &&
                        trimmed.contains("PlayerActionController.start(")
                val isRemovedInteractionRuntimeSymbol =
                    trimmed.contains("WalkToTask") ||
                        trimmed.contains("walkToTask")
                val isRemovedLegacyActionTimerSymbol =
                    trimmed.contains("actionTimer")
                val isRemovedPlayerTickPosting =
                    trimmed.contains("post(PlayerTickEvent(") ||
                        trimmed.contains("GameEventBus.post(PlayerTickEvent(")
                val isLegacyStaticSkillTableBlock =
                    (normalized.endsWith("/content/skills/smithing/SmithingDefinitions.kt") &&
                        (
                            trimmed.contains("smeltingRecipes {") ||
                                trimmed.contains("classicSmeltingButtonMappings") ||
                                trimmed.contains("smithingPageSlots = mapOf(") ||
                                trimmed.contains("buildTier(")
                            )) ||
                        (normalized.endsWith("/content/skills/farming/FarmingDefinitions.kt") &&
                            (
                                trimmed.contains("enum class allotmentPatch") ||
                                    trimmed.contains("enum class flowerPatch") ||
                                    trimmed.contains("enum class herbPatch") ||
                                    trimmed.contains("enum class bushPatch") ||
                                    trimmed.contains("enum class fruitTreePatch") ||
                                    trimmed.contains("enum class treePatch")
                                )) ||
                        (normalized.endsWith("/content/skills/thieving/ThievingDefinition.kt") &&
                            trimmed.contains("enum class ThievingDefinition("))
                val isLegacyRef =
                    trimmed.contains("net.dodian.jobs.") ||
                        trimmed.contains("net.dodian.uber.game.skills.farming.FarmingProcessor") ||
                        trimmed.contains("net.dodian.uber.game.skills.thieving.plunder.PlunderDoorProcessor") ||
                        trimmed.contains("net.dodian.utilities.DatabaseKt") ||
                        trimmed.contains("net.dodian.utilities.DatabaseInitializerKt") ||
                        trimmed.contains("net.dodian.utilities.DotEnvKt") ||
                        removedSkillSymbols.any { symbol ->
                            trimmed.contains(symbol)
                        } ||
                        isLegacyLoopMarker ||
                        removedNpcManagerSymbols.any { symbol ->
                            trimmed.contains(symbol)
                        } ||
                        removedToggleSymbols.any { symbol ->
                            trimmed.contains("import net.dodian.uber.game.config.$symbol") ||
                                trimmed.contains("import static net.dodian.uber.game.config.DotEnvKt.get${symbol.replaceFirstChar { c -> c.uppercaseChar() }}")
                        } ||
                        isWave2LegacyLoopMarker ||
                        isLegacyPlayerArrayAccess ||
                        isHardCutLegacyNaming ||
                        isLegacyFrameApiUsage ||
                        isLegacyClientItemHelperUsage
                        || isManualCoreSkillControllerMarker ||
                        isRemovedInteractionRuntimeSymbol ||
                        isRemovedLegacyActionTimerSymbol ||
                        isRemovedPlayerTickPosting ||
                        isLegacyStaticSkillTableBlock
                if (!isLegacyRef) return@mapIndexedNotNull null
                "${file}:${idx + 1} -> $trimmed"
            }
        }

        val violations = legacyPackageViolations + legacyPathViolations + legacyReferenceViolations
        assertTrue(
            violations.isEmpty(),
            "Legacy repackaged namespaces/paths must not remain.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `plugin index generation uses top-level ksp processor`() {
        val repoRoot = Paths.get("..").normalize().toAbsolutePath()
        val rootSettings = repoRoot.resolve("settings.gradle.kts")
        val serverBuild = repoRoot.resolve("game-server/build.gradle.kts")
        val legacyModuleDir = repoRoot.resolve("game-plugin-index-processor")
        val generatedIndexSource = repoRoot.resolve("game-server/src/main/kotlin/net/dodian/uber/game/plugin/GeneratedPluginModuleIndex.kt")

        val settingsText = Files.readString(rootSettings)
        val serverBuildText = Files.readString(serverBuild)

        val violations = mutableListOf<String>()
        if (!settingsText.contains("include(\":ksp-processor\")")) {
            violations += "settings.gradle.kts must include :ksp-processor"
        }
        if (settingsText.contains("include(\":game-plugin-index-processor\")")) {
            violations += "settings.gradle.kts must not include :game-plugin-index-processor"
        }
        if (!serverBuildText.contains("id(\"com.google.devtools.ksp\")")) {
            violations += "game-server/build.gradle.kts must apply com.google.devtools.ksp"
        }
        if (!serverBuildText.contains("ksp(project(\":ksp-processor\"))")) {
            violations += "game-server/build.gradle.kts must depend on ksp(project(\":ksp-processor\"))"
        }
        if (serverBuildText.contains("generatePluginModuleIndex")) {
            violations += "legacy JavaExec generatePluginModuleIndex task must be removed"
        }
        if (Files.exists(legacyModuleDir)) {
            violations += "legacy module directory game-plugin-index-processor must not exist"
        }
        if (Files.exists(generatedIndexSource)) {
            violations += "GeneratedPluginModuleIndex.kt must not be hand-maintained under src/main"
        }

        assertTrue(
            violations.isEmpty(),
            "Plugin index generation must be KSP-driven.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `required content toml files for migrated skills exist`() {
        val repoRoot = Paths.get("..").normalize().toAbsolutePath()
        val requiredToml = listOf(
            "game-server/src/main/resources/content/skills/cooking.toml",
            "game-server/src/main/resources/content/skills/fishing.toml",
            "game-server/src/main/resources/content/skills/fletching.toml",
            "game-server/src/main/resources/content/skills/woodcutting.toml",
            "game-server/src/main/resources/content/skills/mining.toml",
            "game-server/src/main/resources/content/skills/crafting.toml",
            "game-server/src/main/resources/content/skills/herblore.toml",
            "game-server/src/main/resources/content/skills/runecrafting.toml",
            "game-server/src/main/resources/content/skills/slayer.toml",
            "game-server/src/main/resources/content/skills/prayer.toml",
            "game-server/src/main/resources/content/skills/smithing.toml",
            "game-server/src/main/resources/content/skills/farming.toml",
            "game-server/src/main/resources/content/skills/thieving.toml",
            "game-server/src/main/resources/content/interfaces/magic.toml",
            "game-server/src/main/resources/content/interfaces/skillguide.toml",
            "game-server/src/main/resources/content/objects/travel.toml",
        )
        val missing = requiredToml.filterNot { Files.exists(repoRoot.resolve(it)) }
        assertTrue(
            missing.isEmpty(),
            "Missing required content TOML files.\n${missing.joinToString("\n")}",
        )
    }

    @Test
    fun `intellij tools module is not wired in repo`() {
        val repoRoot = Paths.get("..").normalize().toAbsolutePath()
        val rootSettings = repoRoot.resolve("settings.gradle.kts")
        val moduleDir = repoRoot.resolve("ub3r-intellij-tools")
        val settingsText = Files.readString(rootSettings)
        val violations = mutableListOf<String>()
        if (settingsText.contains("include(\":ub3r-intellij-tools\")")) {
            violations += "settings.gradle.kts must not include :ub3r-intellij-tools"
        }
        if (Files.exists(moduleDir)) {
            violations += "ub3r-intellij-tools directory must not exist"
        }
        assertTrue(
            violations.isEmpty(),
            "IntelliJ tools rollback must be complete.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `combat preemption is centralized in cancellation service`() {
        val cancellationService = sourceRoot.resolve("kotlin/net/dodian/uber/game/systems/action/PlayerActionCancellationService.kt")
        val interactionScheduler = sourceRoot.resolve("kotlin/net/dodian/uber/game/systems/interaction/scheduler/InteractionTaskScheduler.kt")

        val cancellationSource = Files.readString(cancellationService)
        val schedulerSource = Files.readString(interactionScheduler)
        val violations = mutableListOf<String>()

        if (!cancellationSource.contains("CombatPreemptionPolicy.preemptCombatIfNeeded(player, reason)")) {
            violations += "PlayerActionCancellationService must call CombatPreemptionPolicy.preemptCombatIfNeeded"
        }
        if (!schedulerSource.contains("if (intent.option == 5)")) {
            violations += "InteractionTaskScheduler must preserve NPC attack option behavior when choosing cancel reason"
        }

        assertTrue(
            violations.isEmpty(),
            "Combat preemption routing must stay centralized and attack-safe.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `engine loop and netty listeners avoid direct database access`() {
        val violations = sourceFiles
            .filter { file ->
                val normalized = file.invariantSeparatorsPathString
                normalized.contains("/net/dodian/uber/game/engine/loop/") ||
                    normalized.contains("/net/dodian/uber/game/engine/processing/") ||
                    normalized.contains("/net/dodian/uber/game/netty/listener/")
            }
            .flatMap { file ->
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (
                        trimmed.contains("getDbConnection(") ||
                        trimmed.contains("dbConnection")
                    ) {
                        "${file}:${idx + 1} -> $trimmed"
                    } else {
                        null
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Hot paths must not access database directly.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `plugin discovery remains compile time without runtime classpath scanning`() {
        val violations = sourceFiles
            .flatMap { file ->
                Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    if (trimmed.contains("ClassGraph") || trimmed.contains("Reflections(")) {
                        "${file}:${idx + 1} -> $trimmed"
                    } else {
                        null
                    }
                }
            }

        assertTrue(
            violations.isEmpty(),
            "Runtime classpath scanning must not be introduced.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `declarative content files exist for migrated modules`() {
        val requiredResources = listOf(
            sourceRoot.resolve("resources/content/skills/cooking.toml"),
            sourceRoot.resolve("resources/content/skills/fishing.toml"),
            sourceRoot.resolve("resources/content/skills/fletching.toml"),
            sourceRoot.resolve("resources/content/skills/thieving.toml"),
            sourceRoot.resolve("resources/content/interfaces/magic.toml"),
            sourceRoot.resolve("resources/content/interfaces/skillguide.toml"),
            sourceRoot.resolve("resources/content/objects/travel.toml"),
        )
        val missing = requiredResources.filterNot { Files.exists(it) }.map { it.toString() }
        assertTrue(
            missing.isEmpty(),
            "Migrated declarative content files must exist.\n${missing.joinToString("\n")}",
        )
    }

    @Test
    fun `migrated definitions are data-registry backed`() {
        val checks = listOf(
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/cooking/CookingDefinitions.kt") to "SkillDataRegistry.cookingRecipes",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/fishing/FishingDefinitions.kt") to "SkillDataRegistry.fishingSpots",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/fletching/FletchingDefinitions.kt") to "SkillDataRegistry.fletchingBowLogs",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/smithing/SmithingDefinitions.kt") to "SkillDataRegistry.smithingData",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/farming/FarmingDefinitions.kt") to "SkillDataRegistry.farmingData",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/skills/thieving/ThievingDefinitions.kt") to "SkillDataRegistry.thievingDefinitions",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/interfaces/magic/MagicComponents.kt") to "InterfaceMappingRegistry.magicData",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/interfaces/skillguide/SkillGuideComponents.kt") to "InterfaceMappingRegistry.skillGuideData",
            sourceRoot.resolve("kotlin/net/dodian/uber/game/content/objects/travel/TravelObjectComponents.kt") to "InterfaceMappingRegistry.travelData",
        )
        val violations = checks.mapNotNull { (path, marker) ->
            val source = Files.readString(path)
            if (!source.contains(marker)) {
                "$path must reference $marker"
            } else {
                null
            }
        }
        assertTrue(
            violations.isEmpty(),
            "Migrated modules must remain registry-backed.\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `registry calls do not use fallback arguments`() {
        val argumentCallPattern = Regex("""(SkillDataRegistry|InterfaceMappingRegistry)\.[A-Za-z0-9_]+\([^)]*[^)\s][^)]*\)""")
        val violations = sourceFiles.flatMap { file ->
            Files.readAllLines(file).mapIndexedNotNull { idx, line ->
                val trimmed = line.trim()
                val match = argumentCallPattern.find(trimmed) ?: return@mapIndexedNotNull null
                if (match.value.endsWith("()")) return@mapIndexedNotNull null
                "${file}:${idx + 1} -> $trimmed"
            }
        }
        assertTrue(
            violations.isEmpty(),
            "Registry calls must use no-arg required accessors.\n${violations.joinToString("\n")}",
        )
    }
}
