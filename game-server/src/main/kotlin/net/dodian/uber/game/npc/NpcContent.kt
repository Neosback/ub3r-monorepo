package net.dodian.uber.game.npc

import net.dodian.uber.game.api.plugin.PluginModuleMetadata
import net.dodian.uber.game.api.plugin.PluginModuleMetadataProvider
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

typealias NpcClickHandler = (Client, Npc) -> Boolean
internal val NO_CLICK_HANDLER: NpcClickHandler = { _, _ -> false }

enum class NpcInteractionSource {
    DSL,
}

interface NpcModule : PluginModuleMetadataProvider {
    val definition: NpcContentDefinition

    override val pluginMetadata: PluginModuleMetadata
        get() = PluginModuleMetadata(
        name = definition.name,
        description = "NPC family for ${definition.npcIds.size} ids",
        version = "1.0.0",
        owner = "gameplay",
        )
}

interface NpcSpawnSource {
    val spawns: List<NpcSpawnDef>
}

interface NpcFamily : NpcModule, NpcSpawnSource {
    val familyName: String
    val primaryId: Int
    val ids: IntArray
    val cacheOverrides: List<NpcCacheOverride>
    val runtimeDefinitions: List<NpcRuntimeDefinition>
}

data class NpcContentDefinition(
    val name: String,
    val npcIds: IntArray,
    val profiles: Set<String> = emptySet(),
    val optionLabels: Map<Int, String> = emptyMap(),
    val interactionSource: NpcInteractionSource = NpcInteractionSource.DSL,
    val onFirstClick: NpcClickHandler = NO_CLICK_HANDLER,
    val onSecondClick: NpcClickHandler = NO_CLICK_HANDLER,
    val onThirdClick: NpcClickHandler = NO_CLICK_HANDLER,
    val onFourthClick: NpcClickHandler = NO_CLICK_HANDLER,
    val onAttack: NpcClickHandler = NO_CLICK_HANDLER,
    val cacheOverrides: List<NpcCacheOverride> = emptyList(),
    val runtimeDefinitions: List<NpcRuntimeDefinition> = emptyList(),
)

fun NpcContentDefinition.optionLabel(option: Int): String? = optionLabels[option]

internal fun NpcContentDefinition.hasInteractionHandlers(): Boolean {
    return onFirstClick !== NO_CLICK_HANDLER ||
        onSecondClick !== NO_CLICK_HANDLER ||
        onThirdClick !== NO_CLICK_HANDLER ||
        onFourthClick !== NO_CLICK_HANDLER ||
        onAttack !== NO_CLICK_HANDLER
}
