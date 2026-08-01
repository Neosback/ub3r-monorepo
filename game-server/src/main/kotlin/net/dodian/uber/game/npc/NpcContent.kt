package net.dodian.uber.game.npc

import net.dodian.uber.game.api.plugin.PluginModuleMetadata
import net.dodian.uber.game.api.plugin.PluginModuleMetadataProvider
import net.dodian.uber.game.model.entity.npc.Npc
import net.dodian.uber.game.model.entity.player.Client

import net.dodian.uber.game.api.interaction.NpcInteractionContext

typealias NpcClickHandler = (Client, Npc) -> Boolean
internal val NO_CLICK_HANDLER: NpcClickHandler = { _, _ -> false }

typealias NpcContextClickHandler = (NpcInteractionContext) -> Boolean
internal val NO_CONTEXT_CLICK_HANDLER: NpcContextClickHandler = { _ -> false }

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
    val serverDefinitions: List<NpcServerDefinition>
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
    val onFirstClickCtx: NpcContextClickHandler = NO_CONTEXT_CLICK_HANDLER,
    val onSecondClickCtx: NpcContextClickHandler = NO_CONTEXT_CLICK_HANDLER,
    val onThirdClickCtx: NpcContextClickHandler = NO_CONTEXT_CLICK_HANDLER,
    val onFourthClickCtx: NpcContextClickHandler = NO_CONTEXT_CLICK_HANDLER,
    val onAttackCtx: NpcContextClickHandler = NO_CONTEXT_CLICK_HANDLER,
    val cacheOverrides: List<NpcCacheOverride> = emptyList(),
    val serverDefinitions: List<NpcServerDefinition> = emptyList(),
)

fun NpcContentDefinition.optionLabel(option: Int): String? = optionLabels[option]

internal fun NpcContentDefinition.hasInteractionHandlers(): Boolean {
    return onFirstClick !== NO_CLICK_HANDLER ||
        onSecondClick !== NO_CLICK_HANDLER ||
        onThirdClick !== NO_CLICK_HANDLER ||
        onFourthClick !== NO_CLICK_HANDLER ||
        onAttack !== NO_CLICK_HANDLER ||
        onFirstClickCtx !== NO_CONTEXT_CLICK_HANDLER ||
        onSecondClickCtx !== NO_CONTEXT_CLICK_HANDLER ||
        onThirdClickCtx !== NO_CONTEXT_CLICK_HANDLER ||
        onFourthClickCtx !== NO_CONTEXT_CLICK_HANDLER ||
        onAttackCtx !== NO_CONTEXT_CLICK_HANDLER
}

fun NpcContentDefinition.handleFirstClick(context: NpcInteractionContext): Boolean =
    if (onFirstClickCtx !== NO_CONTEXT_CLICK_HANDLER) onFirstClickCtx(context)
    else onFirstClick(context.player, context.npc)

fun NpcContentDefinition.handleSecondClick(context: NpcInteractionContext): Boolean =
    if (onSecondClickCtx !== NO_CONTEXT_CLICK_HANDLER) onSecondClickCtx(context)
    else onSecondClick(context.player, context.npc)

fun NpcContentDefinition.handleThirdClick(context: NpcInteractionContext): Boolean =
    if (onThirdClickCtx !== NO_CONTEXT_CLICK_HANDLER) onThirdClickCtx(context)
    else onThirdClick(context.player, context.npc)

fun NpcContentDefinition.handleFourthClick(context: NpcInteractionContext): Boolean =
    if (onFourthClickCtx !== NO_CONTEXT_CLICK_HANDLER) onFourthClickCtx(context)
    else onFourthClick(context.player, context.npc)

fun NpcContentDefinition.handleAttack(context: NpcInteractionContext): Boolean =
    if (onAttackCtx !== NO_CONTEXT_CLICK_HANDLER) onAttackCtx(context)
    else onAttack(context.player, context.npc)
