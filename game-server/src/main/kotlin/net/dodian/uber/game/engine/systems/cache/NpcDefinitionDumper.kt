package net.dodian.uber.game.engine.systems.cache

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.nio.file.Path

object NpcDefinitionDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = if (args.isNotEmpty()) args[0] else "data/cache"
        val outputPath = "data/def/npc/npc_definitions.json"

        val root = Path.of(cachePath)
        if (!root.toFile().isDirectory) {
            println("Cache path not found: $cachePath")
            return
        }

        println("Opening cache at $cachePath...")
        val store = CacheStore(root).open()

        println("Decoding NPC definitions...")
        val result = NpcCacheDefinitionDecoder.decode(store)

        val npcs = result.entries
            .sortedBy { it.key }
            .map { (id, def) ->
                JsonObject().apply {
                    addProperty("id", id)
                    addProperty("name", def.name)
                    addProperty("examine", def.examine)
                    addProperty("size", def.size)
                    addProperty("combatLevel", def.combatLevel)
                    addProperty("standingAnimation", def.standingAnimation)
                    addProperty("walkingAnimation", def.walkingAnimation)
                    addProperty("halfTurnAnimation", def.halfTurnAnimation)
                    addProperty("clockwiseTurnAnimation", def.clockwiseTurnAnimation)
                    addProperty("anticlockwiseTurnAnimation", def.anticlockwiseTurnAnimation)
                    add("actions", GsonBuilder().create().toJsonTree(def.actions))
                    addProperty("transformVarbit", def.transformVarbit)
                    addProperty("transformVarp", def.transformVarp)
                    if (def.transformChildren.isNotEmpty()) {
                        add("transformChildren", GsonBuilder().create().toJsonTree(def.transformChildren))
                    }
                    addProperty("transformFallbackChild", def.transformFallbackChild)
                }
            }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val gson = GsonBuilder().setPrettyPrinting().create()
        outputFile.writeText(gson.toJson(npcs))

        println("Dumped ${npcs.size} NPC definitions to $outputPath")
    }
}
