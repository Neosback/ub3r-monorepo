package net.dodian.uber.game.content

import net.dodian.uber.game.content.platform.PluginModuleMetadata
import net.dodian.uber.game.content.platform.ModuleConfigRegistry
import net.dodian.uber.game.plugin.GeneratedPluginModuleIndex

object ContentModuleIndex {
    val interfaceButtons
        get() = GeneratedPluginModuleIndex.interfaceButtons.filter { ModuleConfigRegistry.get(it::class.java.name).enabled }

    val objectContents
        get() = GeneratedPluginModuleIndex.objectContents.filter { ModuleConfigRegistry.get(it.second::class.java.name).enabled }

    val itemContents
        get() = GeneratedPluginModuleIndex.itemContents.filter { ModuleConfigRegistry.get(it::class.java.name).enabled }

    val npcContents
        get() = GeneratedPluginModuleIndex.npcContentModules.filter { ModuleConfigRegistry.get(it.first).enabled }.map { it.second }

    val eventBootstraps
        get() = GeneratedPluginModuleIndex.eventBootstrapModules.filter { ModuleConfigRegistry.get(it.first).enabled }.map { it.second }

    @JvmField
    val moduleMetadata: List<PluginModuleMetadata> = GeneratedPluginModuleIndex.moduleMetadata
}
