package net.dodian.uber.game.engine.systems.world.npc

import java.nio.file.Path
import java.util.TreeMap
import net.dodian.uber.game.npc.NpcSpawnDef
import net.dodian.uber.game.npc.NpcDefinitionRepository
import net.dodian.uber.game.npc.NpcServerDefinition
import net.dodian.uber.game.npc.NpcSpawnSource
import net.dodian.uber.game.api.plugin.ContentModuleIndex
import net.dodian.uber.game.model.Position
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.npc.NpcData
import net.dodian.uber.game.model.entity.player.Client
import net.dodian.uber.game.persistence.world.npc.NpcDataRepository
import net.dodian.uber.game.engine.systems.interaction.npcs.NpcContentRegistry
import net.dodian.uber.game.engine.systems.interaction.npcs.NpcInteractionProfileRegistry
import org.slf4j.LoggerFactory

class NpcManager {
    val npcMap = HashMap<Int, Npc>()
    private val data = HashMap<Int, NpcData>()
    private val serverDefinitions = HashMap<Int, NpcServerDefinition>()
    private var nextIndex = 1

    init {
        loadData()
    }

    fun getNpcs(): Collection<Npc> = npcMap.values

    fun getNpcData(): Collection<NpcData> = data.values

    fun loadSpawns() {
        logger.info("Loading NPC spawns from Kotlin modules")
        val kotlinSpawns = ContentModuleIndex.npcModules
            .filterIsInstance<NpcSpawnSource>()
            .flatMap { it.spawns }
        ensureDefinitionsForSpawnNpcIds(kotlinSpawns)
        val loaded = loadContentSpawns(kotlinSpawns)
        logger.info("Loaded {} Kotlin NPC spawns.", loaded)
    }

    private fun ensureDefinitionsForSpawnNpcIds(spawns: List<NpcSpawnDef>) {
        val missingIds = spawns.asSequence().map { it.npcId }.distinct().filter { getData(it) == null }.sorted().toList()
        if (missingIds.isNotEmpty()) {
            error("Missing NPC definitions for Kotlin spawns: ${missingIds.joinToString(",")}")
        }
    }

    private fun loadContentSpawns(contentSpawns: List<NpcSpawnDef>): Int {
        val total = contentSpawns.size
        var loaded = 0
        var skipped = 0
        var skippedInactive = 0
        var skippedDuplicatePosition = 0
        var skippedMissingData = 0
        var failed = 0
        val missingDataByNpcId = TreeMap<Int, Int>()
        val missingSpawnSamplesByNpcId = HashMap<Int, MutableList<Position>>()
        val seen = HashSet<String>(total)

        for (spawn in contentSpawns) {
            try {
                if (!spawn.live) {
                    skipped++
                    skippedInactive++
                    continue
                }
                if (getData(spawn.npcId) == null) {
                    skipped++
                    skippedMissingData++
                    missingDataByNpcId.merge(spawn.npcId, 1, Int::plus)
                    missingSpawnSamplesByNpcId
                        .computeIfAbsent(spawn.npcId) { ArrayList(3) }
                        .takeIf { it.size < 3 }
                        ?.add(Position(spawn.x, spawn.y, spawn.z))
                    continue
                }
                val position = Position(spawn.x, spawn.y, spawn.z)
                val key = "${spawn.npcId}:${spawn.x}:${spawn.y}:${spawn.z}"
                if (!seen.add(key) || hasSpawnAt(spawn.npcId, position)) {
                    skipped++
                    skippedDuplicatePosition++
                    continue
                }
                val npc = createNpc(spawn.npcId, position, spawn.face)
                NpcInteractionProfileRegistry.register(spawn.profile)
                npc.interactionProfile = spawn.profile
                npc.applySpawnOverrides(
                    spawn.overrides.respawnTicks,
                    spawn.overrides.attack,
                    spawn.overrides.defence,
                    spawn.overrides.strength,
                    spawn.overrides.hitpoints,
                    spawn.overrides.ranged,
                    spawn.overrides.magic,
                    spawn.overrides.attackAnimation,
                    spawn.overrides.defenceAnimation,
                    spawn.overrides.deathAnimation,
                )
                npc.applyDisplayOverrides(spawn.overrides.headIcon, spawn.overrides.transformTo)
                npc.applySpawnBehaviorOverrides(
                    effectiveWalkRadius(spawn),
                    spawn.attackRange,
                    spawn.leashDistance,
                    spawn.alwaysActive,
                    spawn.condition,
                )
                val serverDef = serverDefinitions[spawn.npcId]
                if (serverDef != null) {
                    npc.setBossAttackHandler(serverDef.bossAttackHandler)
                    npc.setBossAttackSpeedOverride(serverDef.attackSpeed)
                }
                loaded++
            } catch (e: RuntimeException) {
                failed++
                logger.error(
                    "Failed to create content NPC spawn (id={}, x={}, y={}, z={}).",
                    spawn.npcId,
                    spawn.x,
                    spawn.y,
                    spawn.z,
                    e,
                )
            }
        }

        if (missingDataByNpcId.isNotEmpty()) {
            logger.warn(
                "Skipped {} content NPC spawns with missing NPC definitions: {}",
                skippedMissingData,
                formatMissingNpcCounts(missingDataByNpcId, missingSpawnSamplesByNpcId),
            )
        }

        logger.info(
            "Loaded {}/{} content NPC spawns (skipped {}, inactive {}, duplicate {}, missing-data {}, failed {}).",
            loaded,
            total,
            skipped,
            skippedInactive,
            skippedDuplicatePosition,
            skippedMissingData,
            failed,
        )
        return loaded
    }

    private fun effectiveWalkRadius(spawn: NpcSpawnDef): Int {
        if (spawn.walkRadius > 0) {
            return spawn.walkRadius
        }
        val radial = maxOf(kotlin.math.abs(spawn.rx), kotlin.math.abs(spawn.ry), kotlin.math.abs(spawn.rx2), kotlin.math.abs(spawn.ry2))
        return maxOf(0, radial)
    }

    private fun formatMissingNpcCounts(
        missingDataByNpcId: Map<Int, Int>,
        missingSpawnSamplesByNpcId: Map<Int, List<Position>>,
    ): String {
        val builder = StringBuilder()
        var first = true
        for ((npcId, count) in missingDataByNpcId) {
            if (!first) {
                builder.append(", ")
            }
            val moduleName = NpcContentRegistry.get(npcId)?.name ?: "unmapped"
            val positions = missingSpawnSamplesByNpcId[npcId]
                .orEmpty()
                .joinToString(" | ") { "(${it.x},${it.y},${it.z})" }
                .ifEmpty { "no-sample" }
            builder
                .append(npcId)
                .append("x")
                .append(count)
                .append("[module=")
                .append(moduleName)
                .append(", samples=")
                .append(positions)
                .append("]")
            first = false
        }
        return builder.toString()
    }

    private fun hasSpawnAt(npcId: Int, position: Position): Boolean {
        for (npc in npcMap.values) {
            if (npc.id != npcId) {
                continue
            }
            val npcPosition = npc.position
            if (npcPosition.x == position.x && npcPosition.y == position.y && npcPosition.z == position.z) {
                return true
            }
        }
        return false
    }

    fun findNpcByIdAtPosition(npcId: Int, x: Int, y: Int, z: Int): Npc? {
        for (npc in npcMap.values) {
            val position = npc.position
            if (npc.id == npcId && position.x == x && position.y == y && position.z == z) {
                return npc
            }
        }
        return null
    }

    fun reloadDrops(c: Client, id: Int) {
        try {
            val npcData = data[id]
            if (npcData == null) {
                c.sendMessage("No npc with id of $id")
                return
            }
            npcData.drops.clear()
            for (drop in NpcDataRepository.loadDropsForNpc(id)) {
                npcData.addDrop(drop.itemId, drop.amountMin, drop.amountMax, drop.percent, drop.rareShout)
            }
            c.sendMessage("Finished reloading all drops for ${npcData.name}")
        } catch (e: RuntimeException) {
            logger.error("NPC drop reload failed for id={}", id, e)
        }
    }

    fun reloadAllData(c: Client, id: Int) {
        try {
            reloadDrops(c, id)
            c.sendMessage("Finished reloading drops for '${getData(id)?.name}' npcs. NPC definitions now come from cache and Kotlin families.")
        } catch (e: RuntimeException) {
            logger.error("NPC full data reload failed for id={}", id, e)
        }
    }

    fun reloadNpcConfig(c: Client, id: Int, table: String, value: String) {
        c.sendMessage("NPC definitions are edited in Kotlin family files now. Drops still use ::r_drops / drop commands.")
    }

    fun loadData() {
        try {
            val cachePath = Path.of("data/cache")
            val cacheDefs = NpcDefinitionRepository.load(cachePath)
            val definitions = cacheDefs.mapValues { (_, runtimeDef) ->
                val cacheDef = runtimeDef.cache
                val serverDefValues = runtimeDef.server
                NpcData(
                    cacheDef.name,
                    cacheDef.examine,
                    serverDefValues.attackAnimation ?: 806,
                    serverDefValues.defenceAnimation ?: 0,
                    serverDefValues.deathAnimation ?: 836,
                    serverDefValues.respawnTicks ?: 60,
                    cacheDef.combatLevel,
                    cacheDef.size,
                    intArrayOf(
                        serverDefValues.defence ?: 0,
                        serverDefValues.attack ?: 0,
                        serverDefValues.strength ?: 0,
                        serverDefValues.hitpoints ?: 0,
                        serverDefValues.ranged ?: 0,
                        0,
                        serverDefValues.magic ?: 0
                    ),
                    serverDefValues.aggressive,
                    serverDefValues.alwaysAggressive,
                    serverDefValues.fightsBack
                )
            }
            data.clear()
            data.putAll(definitions)
            serverDefinitions.clear()
            serverDefinitions.putAll(cacheDefs.mapValues { (_, definition) -> definition.server })
            logger.info("Loaded {} NPC definitions from cache plus Kotlin server definitions", definitions.size)
        } catch (e: RuntimeException) {
            logger.error("Error loading NPC definitions", e)
            throw e
        }

        try {
            val dropsByNpc = NpcDataRepository.loadAllDropsByNpcId()
            var amount = 0
            for ((id, drops) in dropsByNpc) {
                val definition = data[id]
                if (definition == null) {
                    logger.warn("Invalid NPC ID for drop: {}", id)
                    continue
                }
                for (drop in drops) {
                    amount++
                    definition.addDrop(drop.itemId, drop.amountMin, drop.amountMax, drop.percent, drop.rareShout)
                }
            }
            logger.info("Loaded {} Npc Drops", amount)
        } catch (e: RuntimeException) {
            logger.error("Error loading NPC drops", e)
        }
    }

    fun createNpc(id: Int, position: Position, face: Int): Npc {
        val npc = Npc(nextIndex, id, position, face)
        serverDefinitions[id]?.let { npc.applyDisplayOverrides(it.headIcon, it.transformTo) }
        npcMap[nextIndex] = npc
        nextIndex++
        if (net.dodian.uber.game.Server.chunkManager != null) {
            npc.syncChunkMembership()
        }
        return npc
    }

    fun removeNpc(npc: Npc) {
        npc.alive = false
        npc.visible = false
        npc.removeFromChunk()
        npcMap.remove(npc.slot)
        NpcTimerScheduler.removeNpc(npc)
    }

    fun getNpc(index: Int): Npc = npcMap[index]
        ?: throw IllegalArgumentException("No NPC loaded at index $index")

    fun getName(id: Int): String = data[id]?.name ?: "NO NPC NAME YET!"

    fun getData(id: Int): NpcData? = data[id]

    companion object {
        private val logger = LoggerFactory.getLogger(NpcManager::class.java)
    }
}
