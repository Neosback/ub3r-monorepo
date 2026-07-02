package net.dodian.uber.game.npc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Files
import java.nio.file.Path
import net.dodian.uber.game.engine.systems.cache.CacheNpcDefinition
import net.dodian.uber.game.engine.systems.cache.CacheVarbitDefinition
import org.slf4j.LoggerFactory

object NpcClientOptionValidator {
    private val logger = LoggerFactory.getLogger(NpcClientOptionValidator::class.java)
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    fun validate(
        rawDefinitions: Map<Int, CacheNpcDefinition>,
        contents: Collection<NpcContentDefinition>,
        modules: Collection<NpcModule> = emptyList(),
        spawns: Collection<NpcSpawnDef> = emptyList(),
    ): NpcClientOptionValidationReport {
        val report = inspect(rawDefinitions, contents, modules, spawns)
        report.warnings.forEach(logger::warn)
        if (report.failures.isNotEmpty()) {
            error("NPC client option validation failed:\n${report.failures.joinToString("\n")}")
        }
        logger.info(
            "NPC client option validation passed: modules={} contentIds={} spawnIds={} interactiveModules={} checkedOptions={} warnings={}",
            report.moduleCount,
            report.contentIdsChecked,
            report.liveSpawnIdsChecked,
            report.interactiveModules,
            report.checkedOptions,
            report.warnings.size,
        )
        return report
    }

    internal fun inspect(
        rawDefinitions: Map<Int, CacheNpcDefinition>,
        contents: Collection<NpcContentDefinition>,
        modules: Collection<NpcModule> = emptyList(),
        spawns: Collection<NpcSpawnDef> = emptyList(),
    ): NpcClientOptionValidationReport {
        val failures = ArrayList<String>()
        val warnings = ArrayList<String>()
        var interactiveModules = 0
        var checkedOptions = 0
        var contentIdsChecked = 0
        val contentByIdentity = contents.associateBy { System.identityHashCode(it) }
        val contentDefinitions = if (contents.isNotEmpty()) contents else modules.map { it.definition }

        for (content in contentDefinitions) {
            for (npcId in content.npcIds.distinct().sorted()) {
                contentIdsChecked++
                val raw = rawDefinitions[npcId]
                if (raw == null) {
                    failures += "${content.name} npc=$npcId is registered in Kotlin content but has no raw client cache definition."
                    continue
                }

                val labels = handlerLabels(content)
                if (labels.isNotEmpty()) {
                    if (contentByIdentity.isEmpty() || contentByIdentity.containsKey(System.identityHashCode(content))) {
                        interactiveModules++
                    }
                    if (raw.name.isNotBlank() && !sameVisibleName(raw.name, content.name)) {
                        warnings += "NPC name mismatch: module=${content.name} npc=$npcId rawName='${raw.name}' handlers=${describeHandlers(content)}. Right-click uses the client/display id name."
                    }
                }

                for ((option, label) in labels) {
                    checkedOptions++
                    if (option == 5) {
                        if (raw.actions.none { it.equals("attack", ignoreCase = true) }) {
                            failures += mismatch(content, npcId, raw, option, label, "choose an NPC id whose client cache exposes Attack")
                        }
                        continue
                    }

                    val rawAction = raw.actions.getOrNull(option - 1)
                    if (rawAction.isNullOrBlank()) {
                        failures += mismatch(content, npcId, raw, option, label, "move this handler to a cache-visible option slot or choose a different NPC id/display id")
                    } else if (!sameVisibleName(rawAction, label)) {
                        warnings += "NPC action label mismatch: module=${content.name} npc=$npcId option=$option label='$label' rawAction='$rawAction'. Right-click text comes from the client cache."
                    }
                }
            }
        }

        val liveSpawnIds = spawns.asSequence()
            .filter { it.live }
            .map { it.npcId }
            .distinct()
            .sorted()
            .toList()
        for (npcId in liveSpawnIds) {
            if (!rawDefinitions.containsKey(npcId)) {
                failures += "Live Kotlin spawn npc=$npcId has no raw client cache definition."
            }
        }

        return NpcClientOptionValidationReport(
            moduleCount = modules.size,
            contentIdsChecked = contentIdsChecked,
            liveSpawnIdsChecked = liveSpawnIds.size,
            interactiveModules = contentDefinitions.count { it.hasInteractionHandlers() },
            checkedOptions = checkedOptions,
            failures = failures,
            warnings = warnings,
        )
    }

    fun writeReports(
        rawDefinitions: Map<Int, CacheNpcDefinition>,
        resolvedDefinitions: Map<Int, CacheNpcDefinition>,
        contents: Collection<NpcContentDefinition>,
        modules: Collection<NpcModule>,
        spawns: Collection<NpcSpawnDef>,
        validation: NpcClientOptionValidationReport,
        varbits: Map<Int, CacheVarbitDefinition> = emptyMap(),
        reportsDir: Path = Path.of("build/reports"),
    ): NpcDiagnosticsReportPaths {
        Files.createDirectories(reportsDir)
        val cachePath = reportsDir.resolve("npc-cache-definitions.json")
        val runtimePath = reportsDir.resolve("npc-runtime-validation.json")
        mapper.writeValue(cachePath.toFile(), rawDefinitions.values.sortedBy { it.id }.map { cacheDumpRow(it, varbits) })
        mapper.writeValue(
            runtimePath.toFile(),
            NpcRuntimeValidationDump(
                summary = NpcRuntimeValidationSummary(
                    rawCacheDefinitions = rawDefinitions.size,
                    modules = modules.size,
                    contentIds = contents.sumOf { it.npcIds.distinct().size },
                    liveSpawns = spawns.count { it.live },
                    uniqueLiveSpawnIds = spawns.asSequence().filter { it.live }.map { it.npcId }.distinct().count(),
                    failures = validation.failures.size,
                    warnings = validation.warnings.size,
                ),
                validation = validation,
                morphVariables = morphVariableDump(rawDefinitions, varbits),
                modules = modules.sortedBy { it.definition.name }.map { moduleDumpRow(it, rawDefinitions, resolvedDefinitions) },
                liveSpawnIds = spawns.asSequence()
                    .filter { it.live }
                    .groupingBy { it.npcId }
                    .eachCount()
                    .toSortedMap()
                    .map { (npcId, count) ->
                        NpcSpawnValidationDump(
                            npcId = npcId,
                            count = count,
                            rawCache = rawDefinitions[npcId]?.let(::cacheSummary),
                            missingRawCache = rawDefinitions[npcId] == null,
                        )
                    },
            ),
        )
        logger.info("NPC diagnostics wrote {} and {}", cachePath, runtimePath)
        return NpcDiagnosticsReportPaths(cachePath, runtimePath)
    }

    private fun moduleDumpRow(
        module: NpcModule,
        rawDefinitions: Map<Int, CacheNpcDefinition>,
        resolvedDefinitions: Map<Int, CacheNpcDefinition>,
    ): NpcModuleValidationDump {
        val definition = module.definition
        return NpcModuleValidationDump(
            module = definition.name,
            primaryId = (module as? NpcFamily)?.primaryId,
            npcIds = definition.npcIds.distinct().sorted(),
            handlers = handlerLabels(definition).map { (option, label) -> NpcHandlerDump(option, label) },
            ids = definition.npcIds.distinct().sorted().map { npcId ->
                NpcIdValidationDump(
                    npcId = npcId,
                    rawCache = rawDefinitions[npcId]?.let(::cacheSummary),
                    resolvedCache = resolvedDefinitions[npcId]?.let(::cacheSummary),
                    cacheDiff = cacheDiff(rawDefinitions[npcId], resolvedDefinitions[npcId]),
                    missingRawCache = rawDefinitions[npcId] == null,
                )
            },
        )
    }

    private fun cacheDumpRow(
        definition: CacheNpcDefinition,
        varbits: Map<Int, CacheVarbitDefinition>,
    ): NpcCacheDefinitionDump =
        NpcCacheDefinitionDump(
            id = definition.id,
            name = definition.name,
            examine = definition.examine,
            size = definition.size,
            combatLevel = definition.combatLevel,
            standingAnimation = definition.standingAnimation,
            walkingAnimation = definition.walkingAnimation,
            halfTurnAnimation = definition.halfTurnAnimation,
            clockwiseTurnAnimation = definition.clockwiseTurnAnimation,
            anticlockwiseTurnAnimation = definition.anticlockwiseTurnAnimation,
            actions = definition.actions.toList(),
            transformVarbit = definition.transformVarbit,
            transformVarp = definition.transformVarp,
            transformChildren = definition.transformChildren,
            transformFallbackChild = definition.transformFallbackChild,
            varbit = definition.transformVarbit.takeIf { it >= 0 }?.let(varbits::get)?.let(::varbitDump),
        )

    private fun cacheSummary(definition: CacheNpcDefinition): NpcCacheSummary =
        NpcCacheSummary(
            name = definition.name,
            examine = definition.examine,
            size = definition.size,
            combatLevel = definition.combatLevel,
            actions = definition.actions.toList(),
            transformVarbit = definition.transformVarbit,
            transformVarp = definition.transformVarp,
            transformChildren = definition.transformChildren,
        )

    private fun varbitDump(definition: CacheVarbitDefinition): NpcVarbitDump =
        NpcVarbitDump(
            id = definition.id,
            varp = definition.varp,
            leastSignificantBit = definition.leastSignificantBit,
            mostSignificantBit = definition.mostSignificantBit,
            maxValue = definition.maxValue,
        )

    private fun morphVariableDump(
        rawDefinitions: Map<Int, CacheNpcDefinition>,
        varbits: Map<Int, CacheVarbitDefinition>,
    ): List<NpcMorphVariableDump> =
        rawDefinitions.values
            .filter { it.hasClientMorph() }
            .groupBy { definition ->
                if (definition.transformVarbit >= 0) {
                    "varbit:${definition.transformVarbit}"
                } else {
                    "varp:${definition.transformVarp}"
                }
            }
            .toSortedMap()
            .map { (key, definitions) ->
                val type = key.substringBefore(':')
                val id = key.substringAfter(':').toInt()
                NpcMorphVariableDump(
                    type = type,
                    id = id,
                    varbit = if (type == "varbit") varbits[id]?.let(::varbitDump) else null,
                    npcIds = definitions.map { it.id }.sorted(),
                    sharedByCount = definitions.size,
                )
            }

    private fun cacheDiff(raw: CacheNpcDefinition?, resolved: CacheNpcDefinition?): Map<String, CacheValueDiff> {
        if (raw == null || resolved == null) return emptyMap()
        val values = linkedMapOf<String, CacheValueDiff>()
        fun add(name: String, before: Any?, after: Any?) {
            if (before != after) values[name] = CacheValueDiff(before, after)
        }
        add("name", raw.name, resolved.name)
        add("examine", raw.examine, resolved.examine)
        add("size", raw.size, resolved.size)
        add("combatLevel", raw.combatLevel, resolved.combatLevel)
        add("standingAnimation", raw.standingAnimation, resolved.standingAnimation)
        add("walkingAnimation", raw.walkingAnimation, resolved.walkingAnimation)
        add("halfTurnAnimation", raw.halfTurnAnimation, resolved.halfTurnAnimation)
        add("clockwiseTurnAnimation", raw.clockwiseTurnAnimation, resolved.clockwiseTurnAnimation)
        add("anticlockwiseTurnAnimation", raw.anticlockwiseTurnAnimation, resolved.anticlockwiseTurnAnimation)
        add("actions", raw.actions.toList(), resolved.actions.toList())
        return values
    }

    private fun handlerLabels(content: NpcContentDefinition): List<Pair<Int, String>> {
        val labels = ArrayList<Pair<Int, String>>(5)
        if (content.onFirstClick !== NO_CLICK_HANDLER) labels += 1 to (content.optionLabel(1) ?: "first")
        if (content.onSecondClick !== NO_CLICK_HANDLER) labels += 2 to (content.optionLabel(2) ?: "second")
        if (content.onThirdClick !== NO_CLICK_HANDLER) labels += 3 to (content.optionLabel(3) ?: "third")
        if (content.onFourthClick !== NO_CLICK_HANDLER) labels += 4 to (content.optionLabel(4) ?: "fourth")
        if (content.onAttack !== NO_CLICK_HANDLER) labels += 5 to (content.optionLabel(5) ?: "attack")
        return labels
    }

    private fun describeHandlers(content: NpcContentDefinition): String =
        handlerLabels(content).joinToString("|") { (option, label) -> "$option:$label" }

    private fun mismatch(
        content: NpcContentDefinition,
        npcId: Int,
        raw: CacheNpcDefinition,
        option: Int,
        label: String,
        hint: String,
    ): String =
        "module=${content.name} npc=$npcId rawName='${raw.name}' registers option=$option label='$label' " +
            "but raw cache actions=${raw.actions.contentToString()}. Hint: $hint."

    private fun sameVisibleName(raw: String, module: String): Boolean =
        normalizeName(raw) == normalizeName(module)

    private fun normalizeName(value: String): String =
        value.replace('_', ' ').trim().lowercase()
}

data class NpcClientOptionValidationReport(
    val moduleCount: Int,
    val contentIdsChecked: Int,
    val liveSpawnIdsChecked: Int,
    val interactiveModules: Int,
    val checkedOptions: Int,
    val failures: List<String>,
    val warnings: List<String>,
) {
    val optionViolations: List<String> get() = failures
    val nameWarnings: List<String> get() = warnings
}

data class NpcDiagnosticsReportPaths(
    val cacheDefinitions: Path,
    val runtimeValidation: Path,
)

data class NpcCacheDefinitionDump(
    val id: Int,
    val name: String,
    val examine: String,
    val size: Int,
    val combatLevel: Int,
    val standingAnimation: Int,
    val walkingAnimation: Int,
    val halfTurnAnimation: Int,
    val clockwiseTurnAnimation: Int,
    val anticlockwiseTurnAnimation: Int,
    val actions: List<String?>,
    val transformVarbit: Int,
    val transformVarp: Int,
    val transformChildren: List<Int>,
    val transformFallbackChild: Int,
    val varbit: NpcVarbitDump?,
)

data class NpcRuntimeValidationDump(
    val summary: NpcRuntimeValidationSummary,
    val validation: NpcClientOptionValidationReport,
    val morphVariables: List<NpcMorphVariableDump>,
    val modules: List<NpcModuleValidationDump>,
    val liveSpawnIds: List<NpcSpawnValidationDump>,
)

data class NpcRuntimeValidationSummary(
    val rawCacheDefinitions: Int,
    val modules: Int,
    val contentIds: Int,
    val liveSpawns: Int,
    val uniqueLiveSpawnIds: Int,
    val failures: Int,
    val warnings: Int,
)

data class NpcModuleValidationDump(
    val module: String,
    val primaryId: Int?,
    val npcIds: List<Int>,
    val handlers: List<NpcHandlerDump>,
    val ids: List<NpcIdValidationDump>,
)

data class NpcHandlerDump(
    val option: Int,
    val label: String,
)

data class NpcIdValidationDump(
    val npcId: Int,
    val rawCache: NpcCacheSummary?,
    val resolvedCache: NpcCacheSummary?,
    val cacheDiff: Map<String, CacheValueDiff>,
    val missingRawCache: Boolean,
)

data class NpcSpawnValidationDump(
    val npcId: Int,
    val count: Int,
    val rawCache: NpcCacheSummary?,
    val missingRawCache: Boolean,
)

data class NpcCacheSummary(
    val name: String,
    val examine: String,
    val size: Int,
    val combatLevel: Int,
    val actions: List<String?>,
    val transformVarbit: Int,
    val transformVarp: Int,
    val transformChildren: List<Int>,
)

data class NpcVarbitDump(
    val id: Int,
    val varp: Int,
    val leastSignificantBit: Int,
    val mostSignificantBit: Int,
    val maxValue: Int,
)

data class NpcMorphVariableDump(
    val type: String,
    val id: Int,
    val varbit: NpcVarbitDump?,
    val npcIds: List<Int>,
    val sharedByCount: Int,
)

data class CacheValueDiff(
    val raw: Any?,
    val resolved: Any?,
)
