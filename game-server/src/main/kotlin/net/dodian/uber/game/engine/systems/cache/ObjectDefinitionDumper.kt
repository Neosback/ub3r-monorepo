package net.dodian.uber.game.engine.systems.cache

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.nio.file.Path

object ObjectDefinitionDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = if (args.isNotEmpty()) args[0] else "data/cache"
        val outputPath = "data/def/object/object_definitions.json"

        val root = Path.of(cachePath)
        if (!root.toFile().isDirectory) {
            println("Cache path not found: $cachePath")
            return
        }

        println("Opening cache at $cachePath...")
        val store = CacheStore(root).open()

        println("Decoding object definitions...")
        val result = ObjectDefinitionDecoder.decode(store)

        val objects = result.definitions.entries
            .sortedBy { it.key }
            .map { (id, def) ->
                JsonObject().apply {
                    addProperty("id", id)
                    addProperty("name", def.name)
                    addProperty("sizeX", def.sizeX)
                    addProperty("sizeY", def.sizeY)
                    addProperty("solid", def.isSolid())
                    addProperty("hasActions", def.hasActions())
                    addProperty("blockWalk", def.blockWalk())
                    addProperty("decoration", def.isDecoration())
                    if (def.childIds != null) add("childIds", GsonBuilder().create().toJsonTree(def.childIds))
                }
            }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val gson = GsonBuilder().setPrettyPrinting().create()
        outputFile.writeText(gson.toJson(objects))

        println("Dumped ${objects.size} object definitions to $outputPath")
    }
}
