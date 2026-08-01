package net.dodian.uber.game.engine.systems.cache

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.nio.file.Path

object InterfaceDefinitionDumper {
    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = if (args.isNotEmpty()) args[0] else "data/cache"
        val outputPath = "data/def/interface/interface_definitions.json"

        val root = Path.of(cachePath)
        if (!root.toFile().isDirectory) {
            println("Cache path not found: $cachePath")
            return
        }

        println("Opening cache at $cachePath...")
        val store = CacheStore(root).open()

        println("Decoding interface definitions...")
        val result = InterfaceDefinitionDecoder.decode(store)

        val gson = GsonBuilder().create()
        val interfaces = result.entries
            .sortedBy { it.key }
            .map { (id, def) ->
                JsonObject().apply {
                    addProperty("id", id)
                    addProperty("parentId", def.parentId)
                    addProperty("type", def.type)
                    addProperty("atActionType", def.atActionType)
                    addProperty("contentType", def.contentType)
                    addProperty("width", def.width)
                    addProperty("height", def.height)
                    if (def.hoverType != -1) addProperty("hoverType", def.hoverType)
                    if (def.tooltip.isNotEmpty()) addProperty("tooltip", def.tooltip)
                    if (def.disabledMessage.isNotEmpty()) addProperty("disabledMessage", def.disabledMessage)
                    if (def.enabledMessage.isNotEmpty()) addProperty("enabledMessage", def.enabledMessage)
                    if (def.disabledSprite.isNotEmpty()) addProperty("disabledSprite", def.disabledSprite)
                    if (def.enabledSprite.isNotEmpty()) addProperty("enabledSprite", def.enabledSprite)
                    if (def.selectedActionName.isNotEmpty()) addProperty("selectedActionName", def.selectedActionName)
                    if (def.spellName.isNotEmpty()) addProperty("spellName", def.spellName)
                    if (def.spellUsableOn != 0) addProperty("spellUsableOn", def.spellUsableOn)
                    if (def.scrollMax != 0) addProperty("scrollMax", def.scrollMax)
                    if (def.modelZoom != 0) addProperty("modelZoom", def.modelZoom)
                    if (def.modelRotation1 != 0) addProperty("modelRotation1", def.modelRotation1)
                    if (def.modelRotation2 != 0) addProperty("modelRotation2", def.modelRotation2)
                    if (def.mediaID != 0) addProperty("mediaID", def.mediaID)
                    val actions = def.actions
                    if (actions != null && actions.any { it != null }) {
                        add("actions", gson.toJsonTree(actions))
                    }
                    val children = def.children
                    if (children != null && children.isNotEmpty()) {
                        add("children", gson.toJsonTree(children))
                        add("childX", gson.toJsonTree(def.childX))
                        add("childY", gson.toJsonTree(def.childY))
                    }
                }
            }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val prettyGson = GsonBuilder().setPrettyPrinting().create()
        outputFile.writeText(prettyGson.toJson(interfaces))

        println("Dumped ${interfaces.size} interface definitions to $outputPath")
    }
}
