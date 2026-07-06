package net.dodian.uber.game.engine.systems.cache

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.dodian.uber.game.rscm.Namer
import java.io.File
import java.io.FileReader
import java.nio.file.Path

object RscmGenerator {

    @JvmStatic
    fun main(args: Array<String>) {
        var outputDir = File("data/mappings")
        if (File("../settings.gradle.kts").exists()) {
            outputDir = File("../game-server/data/mappings")
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val gson = Gson()
        val cachePath = "data/cache"
        val cacheDir = File(cachePath).let { if (it.exists()) it else File("game-server/$cachePath") }
        if (!cacheDir.isDirectory) {
            println("Cache directory not found: ${cacheDir.absolutePath}")
            return
        }

        println("Opening cache at ${cacheDir.absolutePath}...")
        val store = CacheStore(cacheDir.toPath()).open()

        // 1. Items -> obj.rscm (reads from JSON)
        generateRscmFile(
            sourceFile = File("data/def/item/item_definitions.json"),
            destFile = File(outputDir, "obj.rscm"),
            gson = gson
        )

        // 2. NPCs -> npc.rscm (decoded from cache)
        println("Decoding NPC definitions...")
        val npcDefs = NpcCacheDefinitionDecoder.decode(store)
        val npcEntries = npcDefs.values
            .sortedWith(compareByDescending<CacheNpcDefinition> { it.actions.filterNotNull().size }
                .thenByDescending { it.id })
            .map { def -> def.name to def.id }
        writeRscmFile(npcEntries, File(outputDir, "npc.rscm"))

        // 3. Objects -> loc.rscm (decoded from cache)
        println("Decoding Object definitions...")
        val objDefs = ObjectDefinitionDecoder.decode(store).definitions
        val objEntries = objDefs.values.map { def -> def.name to def.id }
        writeRscmFile(objEntries, File(outputDir, "loc.rscm"))

        // 4. SpotAnims -> spotanim.rscm (decoded from cache, names from rsmod spotanim.sym)
        println("Decoding SpotAnim definitions...")
        val spotAnimDefs = SpotAnimDefinitionDecoder.decode(store)
        val spotAnimSymFile = File("/Users/tylercovalt/Desktop/rsmod-main/.data/symbols/spotanim.sym")
        val spotAnimNames = parseSymFile(spotAnimSymFile)
        println("Loaded ${spotAnimNames.size} spotanim names from ${spotAnimSymFile.path}")

        val spotAnimEntries = spotAnimDefs.keys.map { id ->
            val name = spotAnimNames[id] ?: "spotanim_$id"
            name to id
        }
        writeRscmFile(spotAnimEntries, File(outputDir, "spotanim.rscm"))

        // 5. Projectiles -> projanim.rscm (from rsmod projanim.sym)
        println("Reading Projectile definitions...")
        val projSymFile = File("/Users/tylercovalt/Desktop/rsmod-main/.data/symbols/projanim.sym")
        val projNames = parseSymFile(projSymFile)
        println("Loaded ${projNames.size} projanim names from ${projSymFile.path}")
        val projEntries = projNames.map { (id, name) -> name to id }
        writeRscmFile(projEntries, File(outputDir, "projanim.rscm"))

        // 6. Sequences -> seq.rscm (decoded from cache, names from rsmod seq.sym up to 10677)
        println("Decoding Sequence definitions...")
        val seqDefs = AnimationDefinitionDecoder.decode(store)
        val seqSymFile = File("/Users/tylercovalt/Desktop/rsmod-main/.data/symbols/seq.sym")
        val seqNames = parseSymFile(seqSymFile)
        println("Loaded ${seqNames.size} seq names from ${seqSymFile.path}")

        val maxSeqId = 10677
        val seqEntries = seqDefs.keys.filter { it <= maxSeqId }.map { id ->
            val name = seqNames[id] ?: "seq_$id"
            name to id
        }
        writeRscmFile(seqEntries, File(outputDir, "seq.rscm"))

        // 7. Varps -> varp.rscm (from VoidPS TOML)
        val voidPsDir = File("/Users/tylercovalt/Desktop/RSPS/game-server-main/data")
        val varps = mutableMapOf<String, Int>()
        if (voidPsDir.isDirectory) {
            println("Scanning VoidPS Varp TOML files...")
            voidPsDir.walkTopDown().filter { it.isFile && it.name.endsWith(".varps.toml") }.forEach { file ->
                parseSectionIdToml(file).forEach { (name, id) ->
                    varps[name] = id
                }
            }
            writeRscmFile(varps.map { it.key to it.value }, File(outputDir, "varp.rscm"))
        }

        // 8. Varbits -> varbit.rscm (from VoidPS TOML)
        val varbits = mutableMapOf<String, Int>()
        if (voidPsDir.isDirectory) {
            println("Scanning VoidPS Varbit TOML files...")
            voidPsDir.walkTopDown().filter { it.isFile && it.name.endsWith(".varbits.toml") }.forEach { file ->
                parseSectionIdToml(file).forEach { (name, id) ->
                    varbits[name] = id
                }
            }
            writeRscmFile(varbits.map { it.key to it.value }, File(outputDir, "varbit.rscm"))
        }

        // 9. Interfaces & Components -> interface.rscm / component.rscm (directly decoded from cache)
        val ifaceNamesFile = File("/Users/tylercovalt/Desktop/RSPS/377.txt")
        if (ifaceNamesFile.exists()) {
            println("Generating Interface mappings from cache and 377 names...")
            generateInterfaceMappings(store, ifaceNamesFile, outputDir)
        } else {
            println("Warning: Interface names file not found at ${ifaceNamesFile.path}")
        }

        println("Successfully generated RSCM mapping files under ${outputDir.absolutePath}!")
    }

    private fun generateInterfaceMappings(store: CacheStore, namesFile: File, outputDir: File) {
        val interfaceMap = mutableMapOf<String, Int>()
        val componentMap = mutableMapOf<String, Int>()
        
        val interfaceExistingKeys = mutableSetOf<String>()
        val componentExistingKeys = mutableSetOf<String>()

        val interfaces = InterfaceDefinitionDecoder.decode(store)
        if (interfaces.isEmpty()) {
            println("Warning: No interface definitions decoded from cache.")
            return
        }

        val cleanInterfaceNames = parse377InterfaceNames(namesFile)
        println("Loaded ${cleanInterfaceNames.size} interface names from ${namesFile.path}")

        fun getRootInterfaceId(id: Int): Int {
            var current = interfaces[id] ?: return id
            var safety = 0
            while (current.parentId != -1 && safety < 100) {
                val parent = interfaces[current.parentId] ?: break
                current = parent
                safety++
            }
            return current.id
        }

        val rootIdToName = mutableMapOf<Int, String>()
        
        // Seed with 377.txt clean names
        for ((id, rawName) in cleanInterfaceNames.entries.sortedBy { it.key }) {
            val key = generateKey(rawName, id, interfaceExistingKeys)
            interfaceMap[key] = id
            rootIdToName[id] = key
        }

        // Add any other root interfaces found in the cache (parentId == -1)
        val cacheRootIds = interfaces.values.filter { it.parentId == -1 }.map { it.id }.sorted()
        for (rootId in cacheRootIds) {
            if (rootIdToName.containsKey(rootId)) continue

            val rootDef = interfaces[rootId]
            var nameToUse = rootDef?.disabledMessage?.trim() ?: ""
            if (nameToUse.isEmpty()) {
                nameToUse = interfaces.values
                    .filter { getRootInterfaceId(it.id) == rootId && it.type == 4 && it.disabledMessage.trim().isNotEmpty() }
                    .minByOrNull { it.id }
                    ?.disabledMessage?.trim() ?: ""
            }

            val key = if (nameToUse.isNotEmpty()) {
                generateKey(nameToUse, rootId, interfaceExistingKeys)
            } else {
                "interface_$rootId"
            }
            interfaceMap[key] = rootId
            rootIdToName[rootId] = key
        }

        // Map child components
        val childDefs = interfaces.values.filter { it.parentId != -1 }.sortedBy { it.id }
        for (def in childDefs) {
            val rootId = getRootInterfaceId(def.id)
            val parentName = rootIdToName[rootId] ?: "interface_$rootId"

            var cleanText = when {
                def.tooltip.isNotEmpty() -> def.tooltip
                def.spellName.isNotEmpty() -> def.spellName
                def.type == 4 && def.disabledMessage.isNotEmpty() -> def.disabledMessage
                else -> ""
            }.trim()

            if (cleanText.isNotEmpty()) {
                cleanText = Namer.removeTags(cleanText)
                    .replace("\n", " ").replace("\r", " ").replace("\t", " ").trim()

                if (cleanText.isNotEmpty()) {
                    val compKey = generateKey(cleanText, def.id, componentExistingKeys)
                    componentMap["$parentName:$compKey"] = def.id
                }
            }
        }

        File(outputDir, "interface.rscm").printWriter().use { writer ->
            for ((key, id) in interfaceMap.entries.sortedBy { it.value }) {
                writer.println("$key=$id")
            }
        }
        println("Generated ${interfaceMap.size} mappings in interface.rscm")

        File(outputDir, "component.rscm").printWriter().use { writer ->
            for ((key, id) in componentMap.entries.sortedBy { it.value }) {
                writer.println("$key=$id")
            }
        }
        println("Generated ${componentMap.size} mappings in component.rscm")
    }

    private fun parse377InterfaceNames(file: File): Map<Int, String> {
        if (!file.exists()) return emptyMap()
        val results = mutableMapOf<Int, String>()
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
            val firstSpace = trimmed.indexOfFirst { it.isWhitespace() }
            if (firstSpace != -1) {
                val idStr = trimmed.substring(0, firstSpace)
                val nameStr = trimmed.substring(firstSpace).trim()
                val id = idStr.toIntOrNull()
                if (id != null && nameStr.isNotEmpty()) {
                    results[id] = nameStr
                }
            }
        }
        return results
    }

    private fun writeRscmFile(rawEntries: List<Pair<String, Int>>, destFile: File) {
        val entries = mutableListOf<Pair<String, Int>>()
        val existingKeys = mutableSetOf<String>()

        for ((rawName, id) in rawEntries) {
            val key = generateKey(rawName, id, existingKeys)
            entries.add(key to id)
        }

        destFile.printWriter().use { writer ->
            for ((key, id) in entries.sortedBy { it.second }) {
                writer.println("$key=$id")
            }
        }
        println("Generated ${entries.size} mappings in ${destFile.name}")
    }

    private fun parseSectionIdToml(file: File): Map<String, Int> {
        val results = mutableMapOf<String, Int>()
        var currentSection: String? = null
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed.substring(1, trimmed.length - 1).trim()
            } else if (currentSection != null && trimmed.startsWith("id")) {
                val parts = trimmed.split("=")
                if (parts.size == 2) {
                    parts[1].trim().toIntOrNull()?.let { id ->
                        results[currentSection!!] = id
                    }
                }
            }
        }
        return results
    }

    private fun parseSymFile(file: File): Map<Int, String> {
        if (!file.exists()) return emptyMap()
        val results = mutableMapOf<Int, String>()
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
            val parts = trimmed.split("\t")
            if (parts.size == 2) {
                val id = parts[0].toIntOrNull()
                val name = parts[1].trim()
                if (id != null && name.isNotEmpty()) {
                    results[id] = name
                }
            }
        }
        return results
    }

    private fun generateRscmFile(sourceFile: File, destFile: File, gson: Gson) {
        var file = sourceFile
        if (!file.exists()) {
            file = File("game-server/${sourceFile.path}")
        }
        if (!file.exists()) {
            println("Warning: Source file not found: ${sourceFile.path} (also checked game-server/${sourceFile.path})")
            return
        }

        val entries = mutableListOf<Pair<String, Int>>()
        val existingKeys = mutableSetOf<String>()

        FileReader(file).use { reader ->
            val jsonArray = gson.fromJson(reader, JsonArray::class.java)
            for (element in jsonArray) {
                if (element is JsonObject) {
                    val id = element.get("id")?.asInt ?: continue
                    val name = element.get("name")?.asString ?: "unnamed"
                    val key = generateKey(name, id, existingKeys)
                    entries.add(key to id)
                }
            }
        }

        destFile.printWriter().use { writer ->
            for ((key, id) in entries.sortedBy { it.second }) {
                writer.println("$key=$id")
            }
        }
        println("Generated ${entries.size} mappings in ${destFile.name}")
    }

    private fun generateKey(name: String, id: Int, existing: MutableSet<String>): String {
        val base = Namer.sanitizeRSCM(name)
        val key = if (base.isEmpty()) "unnamed" else base
        if (existing.add(key)) {
            return key
        }
        val suffixKey = "${key}_$id"
        existing.add(suffixKey)
        return suffixKey
    }
}






