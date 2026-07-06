package net.dodian.uber.game.engine.systems.cache

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.nio.file.Path

object SpotAnimDefinitionDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = if (args.size > 0) args[0] else "data/cache"
        val symPath   = if (args.size > 1) args[1] else null
        val outputPath = "data/def/spotanim/spotanim_definitions.json"

        val root = Path.of(cachePath)
        if (!root.toFile().isDirectory) {
            println("Cache path not found: $cachePath")
            return
        }

        // Load rsmod spotanim.sym: format is "<id>\t<name>" per line
        val nameMap = mutableMapOf<Int, String>()
        if (symPath != null) {
            val symFile = File(symPath)
            if (symFile.exists()) {
                symFile.forEachLine { line ->
                    val parts = line.trim().split("\t")
                    if (parts.size == 2) {
                        parts[0].toIntOrNull()?.let { id -> nameMap[id] = parts[1] }
                    }
                }
                println("Loaded ${nameMap.size} spotanim names from $symPath")
            } else {
                println("Warning: sym file not found at $symPath, names will be omitted")
            }
        }

        println("Opening cache at $cachePath...")
        val store = CacheStore(root).open()

        println("Decoding spot animation definitions...")
        val result = SpotAnimDefinitionDecoder.decode(store)

        val gson = GsonBuilder().create()
        var named = 0
        val spotAnims = result.entries
            .sortedBy { it.key }
            .map { (id, def) ->
                JsonObject().apply {
                    addProperty("id", id)
                    val name = nameMap[id]
                    if (name != null) { addProperty("name", name); named++ }
                    addProperty("modelId", def.modelId)
                    addProperty("animationId", def.animationId)
                    addProperty("resizeXY", def.resizeXY)
                    addProperty("resizeZ", def.resizeZ)
                    addProperty("rotation", def.rotation)
                    addProperty("modelBrightness", def.modelBrightness)
                    addProperty("modelShadow", def.modelShadow)
                    if (def.originalModelColours.isNotEmpty()) {
                        add("originalModelColours", gson.toJsonTree(def.originalModelColours))
                        add("modifiedModelColours", gson.toJsonTree(def.modifiedModelColours))
                    }
                }
            }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val prettyGson = GsonBuilder().setPrettyPrinting().create()
        outputFile.writeText(prettyGson.toJson(spotAnims))

        println("Dumped ${spotAnims.size} spot animation definitions to $outputPath ($named named, ${spotAnims.size - named} unnamed)")
    }
}
